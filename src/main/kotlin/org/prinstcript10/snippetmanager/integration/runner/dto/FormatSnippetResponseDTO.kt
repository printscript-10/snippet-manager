package org.prinstcript10.snippetmanager.integration.runner.dto

data class FormatSnippetResponseDTO(
    val formattedSnippet: String?,
    val errors: List<String>,
)
