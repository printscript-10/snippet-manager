package org.prinstcript10.snippetmanager.testCase.model.dto

import org.prinstcript10.snippetmanager.testCase.model.enum.TestStatus

data class TestCaseDTO(
    val id: String,
    val name: String,
    val input: List<String>,
    val output: List<String>,
    val status: TestStatus?,
)
