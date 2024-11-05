package org.prinstcript10.snippetmanager.testCase.model.dto

import jakarta.validation.constraints.NotBlank

data class CreateTestCaseDTO(
    @NotBlank(message = "Name should not be empty")
    val name: String,
    val inputs: List<String>,
    val outputs: List<String>
)
