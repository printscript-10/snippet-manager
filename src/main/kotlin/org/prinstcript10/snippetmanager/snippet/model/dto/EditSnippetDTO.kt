package org.prinstcript10.snippetmanager.snippet.model.dto

import jakarta.validation.constraints.NotBlank

data class EditSnippetDTO(
    @NotBlank(message = "Snippet is required")
    val snippet: String = "",
)
