package org.prinstcript10.snippetmanager.testCase.service

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import org.prinstcript10.snippetmanager.integration.asset.AssetService
import org.prinstcript10.snippetmanager.integration.permission.PermissionService
import org.prinstcript10.snippetmanager.integration.permission.SnippetOwnership
import org.prinstcript10.snippetmanager.integration.permission.SnippetPermissionDTO
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.shared.exception.BadRequestException
import org.prinstcript10.snippetmanager.shared.exception.ConflictException
import org.prinstcript10.snippetmanager.shared.exception.NotFoundException
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage
import org.prinstcript10.snippetmanager.snippet.repository.SnippetRepository
import org.prinstcript10.snippetmanager.snippet.repository.TestCaseRepository
import org.prinstcript10.snippetmanager.testCase.model.dto.CreateTestCaseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.RunTestCaseResponseDTO
import org.prinstcript10.snippetmanager.testCase.model.entity.TestCase
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class TestCaseService (
    @Autowired
    private val testCaseRepository: TestCaseRepository,
    private val permissionService: PermissionService,
    private val runnerServices: Map<SnippetLanguage, RunnerService>,
    private val snippetRepository: SnippetRepository
) {

    fun addTestCase(createTestCaseDTO: CreateTestCaseDTO, snippetId: String, token: String) {
        val permission = permissionService.getPermission(snippetId, token)

        val ownership = (permission.body as SnippetPermissionDTO).ownership

        if (ownership != SnippetOwnership.OWNER) {
            throw BadRequestException(
                "User is not the snippet owner",
            )
        }

        val snippet = snippetRepository.findById(snippetId)
            .orElseThrow { throw NotFoundException("Snippet not found with id: $snippetId") }

        val testCase = TestCase(
            name = createTestCaseDTO.name,
            input = createTestCaseDTO.inputs,
            output = createTestCaseDTO.outputs,
            snippet = snippet
        )

        testCaseRepository.save(testCase)
    }

    fun runTestCase(testId: String, token: String): RunTestCaseResponseDTO {
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
            errors = runnerResponse.body!!.errors
        )

        if (runnerResponse.body!!.outputs != test.output || runnerResponse.body!!.errors.isNotEmpty()){
            response.success = false
        }
        return response

    }
}
