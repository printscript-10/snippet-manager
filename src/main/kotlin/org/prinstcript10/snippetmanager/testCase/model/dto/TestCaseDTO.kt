package org.prinstcript10.snippetmanager.testCase.model.dto

data class TestCaseDTO(
    val id: String,
    val name: String,
    val input: List<String>,
    val output: List<String>,
)
