package org.prinstcript10.snippetmanager.integration.runner.dto

data class RunSnippetResponseDTO(
    val outputs: List<String>,
    val errors: List<String>,
)
