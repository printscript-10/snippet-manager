package org.prinstcript10.snippetmanager.testCase.controller

import jakarta.validation.Valid
import org.prinstcript10.snippetmanager.testCase.model.dto.CreateTestCaseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.RunTestCaseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.RunTestCaseResponseDTO
import org.prinstcript10.snippetmanager.testCase.model.dto.TestCaseDTO
import org.prinstcript10.snippetmanager.testCase.service.TestCaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("tests")
@Validated
class TestCaseController(
    @Autowired
    private val testCaseService: TestCaseService,
) {
    @PostMapping("/{snippetId}")
    fun addTest(
        @PathVariable("snippetId") snippetId: String,
        @Valid @RequestBody createTestCaseDTO: CreateTestCaseDTO,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        return testCaseService.addTestCase(createTestCaseDTO, snippetId, jwt.tokenValue)
    }

    @PutMapping()
    fun runTest(
        @Valid @RequestBody runTestCaseDTO: RunTestCaseDTO,
        @AuthenticationPrincipal jwt: Jwt,
    ): RunTestCaseResponseDTO {
        return testCaseService.runTestCase(runTestCaseDTO, jwt.tokenValue)
    }

    @PutMapping("/{testId}")
    fun runTestById(
        @PathVariable("testId") testId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): RunTestCaseResponseDTO {
        return testCaseService.runTestCaseById(testId, jwt.tokenValue)
    }

    @DeleteMapping("/{testId}")
    fun deleteTest(
        @PathVariable("testId") testId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        return testCaseService.deleteTestCase(testId, jwt.tokenValue)
    }

    @GetMapping("/{snippetId}")
    fun getSnippetTests(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<TestCaseDTO> {
        return testCaseService.getSnippetTests(snippetId, jwt.tokenValue)
    }
}
