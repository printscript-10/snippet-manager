package org.prinstcript10.snippetmanager.testCase.model.dto

data class RunTestCaseDTO(
    val snippetId: String,
    val input: List<String>?,
    val output: List<String>?,
)
