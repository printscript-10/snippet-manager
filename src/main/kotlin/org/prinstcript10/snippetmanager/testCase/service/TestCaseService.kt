package org.prinstcript10.snippetmanager.testCase.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.hibernate.Hibernate
import org.prinstcript10.snippetmanager.integration.permission.PermissionService
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.redis.testCase.event.TestCaseRequestEvent
import org.prinstcript10.snippetmanager.redis.testCase.producer.TestCaseRequestProducer
import org.prinstcript10.snippetmanager.shared.exception.BadRequestException
import org.prinstcript10.snippetmanager.shared.exception.ConflictException
import org.prinstcript10.snippetmanager.shared.exception.NotFoundException
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage
import org.prinstcript10.snippetmanager.snippet.repository.SnippetRepository
import org.prinstcript10.snippetmanager.snippet.repository.SnippetTestingRepository
import org.prinstcript10.snippetmanager.snippet.repository.TestCaseRepository
import org.prinstcript10.snippetmanager.testCase.model.dto.CreateTestCaseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.RunTestCaseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.RunTestCaseResponseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.TestCaseDTO
import org.prinstcript10.snippetmanager.testCase.model.entity.SnippetTesting
import org.prinstcript10.snippetmanager.testCase.model.entity.TestCase
import org.prinstcript10.snippetmanager.testCase.model.enum.TestStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestCaseService(
    @Autowired
    private val testCaseRepository: TestCaseRepository,
    private val permissionService: PermissionService,
    private val runnerServices: Map<SnippetLanguage, RunnerService>,
    private val snippetRepository: SnippetRepository,
    private val objectMapper: ObjectMapper,
    private val snippetTestingRepository: SnippetTestingRepository,
    private val testCaseRequestProducer: TestCaseRequestProducer,

) {

    private val logger = LoggerFactory.getLogger(TestCaseService::class.java)

    fun addTestCase(createTestCaseDTO: CreateTestCaseDTO, snippetId: String, token: String) {
        logger.info("Creating test case: $createTestCaseDTO for snippet: $snippetId")
        permissionService.getPermission(snippetId, token)

//        val ownership = (permission.body as SnippetPermissionDTO).ownership
//
//        if (ownership != SnippetOwnership.OWNER) {
//            throw BadRequestException(
//                "User is not the snippet owner",
//            )
//        }

        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { throw NotFoundException("Snippet not found with id: $snippetId") }

        val testCase = TestCase(
            name = createTestCaseDTO.name,
            input = createTestCaseDTO.inputs ?: listOf(),
            output = createTestCaseDTO.outputs ?: listOf(),
            snippet = snippet,
        )

        val savedTestCase = testCaseRepository.save(testCase)

        createPendingSnippetTest(savedTestCase)
    }

    fun runTestCaseById(testId: String, token: String): RunTestCaseResponseDTO {
        logger.info("Running test case with id: $testId")
        val test = testCaseRepository.findById(testId).orElseThrow {
            BadRequestException("No TestCase with that Id found")
        }

        val snippetId = test.snippet?.id
            ?: throw ConflictException("Error finding the snippet for that test")

        val runnerResponse = runnerServices[test.snippet.language]!!.runSnippet(
            test.input,
            snippetId,
            token,
        )

        val response = RunTestCaseResponseDTO(
            success = true,
            inputs = test.input,
            expectedOutputs = test.output,
            actualOutputs = runnerResponse.body!!.outputs,
            errors = runnerResponse.body!!.errors,
        )

        if (runnerResponse.body!!.outputs != test.output || runnerResponse.body!!.errors.isNotEmpty()) {
            response.success = false
        }
        return response
    }

    fun runTestCase(testCaseDTO: RunTestCaseDTO, token: String): RunTestCaseResponseDTO {
        logger.info("Running test for snippet: ${testCaseDTO.snippetId}")
        val snippet = snippetRepository.findById(testCaseDTO.snippetId)
            .orElseThrow { throw NotFoundException("Snippet not found with id: ${testCaseDTO.snippetId}") }

        val runnerResponse = runnerServices[snippet.language]!!.runSnippet(
            testCaseDTO.input ?: listOf(),
            snippet.id!!,
            token,
        )

        val response = RunTestCaseResponseDTO(
            success = true,
            inputs = testCaseDTO.input ?: listOf(),
            expectedOutputs = testCaseDTO.output ?: listOf(),
            actualOutputs = runnerResponse.body!!.outputs,
            errors = runnerResponse.body!!.errors,
        )

        if (runnerResponse.body!!.outputs != testCaseDTO.output || runnerResponse.body!!.errors.isNotEmpty()) {
            response.success = false
        }
        return response
    }

    @Transactional
    fun getTestCaseById(id: String): TestCase {
        logger.info("Getting test case by id: $id")
        val testCase = testCaseRepository.findById(id)
            .orElseThrow { NotFoundException("TestCase with ID $id not found") }

        Hibernate.initialize(testCase.output)
        return testCase
    }

    fun getSnippetTests(snippetId: String, token: String): List<TestCaseDTO> {
        logger.info("Getting snippet tests for snippet: $snippetId")
        snippetRepository.findById(snippetId)
            .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

        return testCaseRepository.findBySnippetId(snippetId).map { testCase ->
            val currentStatus = testCase.snippetTesting.firstOrNull()?.status ?: TestStatus.PENDING

            TestCaseDTO(
                id = testCase.id!!,
                name = testCase.name,
                input = testCase.input,
                output = testCase.output,
                status = currentStatus,
            )
        }
    }

    fun deleteTestCase(testId: String, token: String) {
        val existingTest = testCaseRepository.findById(testId)
            .orElseThrow { NotFoundException("Snippet with ID $testId not found") }

        val snippetId = existingTest.snippet?.id
            ?: throw ConflictException("Error finding the snippet for that test")
        logger.info("Getting permission for snippet: $snippetId")
        permissionService.getPermission(snippetId, token)
        logger.info("Deleting test case: $testId")
        testCaseRepository.deleteById(testId)
    }

    fun resetSnippetTesting(snippetId: String): List<String> {
        logger.info("Resetting snippet testing status for snippet: $snippetId")
        val tests = snippetTestingRepository.findAllBySnippetId(snippetId)
        tests.forEach {
            it.status = TestStatus.PENDING
        }

        snippetTestingRepository.saveAll(tests)

        return tests.map { it.testCase!!.id!! }
    }

    private fun createPendingSnippetTest(testCase: TestCase) {
        snippetTestingRepository.save(
            SnippetTesting(
                testCase = testCase,
                status = TestStatus.PENDING,
            ),
        )
    }

    suspend fun publishSnippetTestingEvents(testIds: List<String>) {
        val tests = testCaseRepository.findAllById(testIds)
        if (tests.isEmpty()) { return }
        tests.forEach {
            testCaseRequestProducer.publishEvent(
                objectMapper.writeValueAsString(
                    TestCaseRequestEvent(
                        testCaseId = it.id!!,
                        snippetId = it.snippet!!.id!!,
                        inputs = it.input,
                    ),
                ),
            )
        }
    }

    @Transactional
    fun updateSnippetTestCaseStatus(testId: String, status: TestStatus) {
        snippetTestingRepository.findFirstByTestCaseId(testId)?.let {
            it.status = status
            snippetTestingRepository.save(it)
        }
    }
}
