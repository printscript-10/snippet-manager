package org.prinstcript10.snippetmanager.testCase.model.dto

data class RunTestCaseResponseDTO (
    var success: Boolean,
    val inputs: List<String>,
    val expectedOutputs: List<String>,
    val actualOutputs: List<String>,
    val errors: List<String>
)
