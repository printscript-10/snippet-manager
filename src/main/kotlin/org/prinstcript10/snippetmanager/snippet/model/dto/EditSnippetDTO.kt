package org.prinstcript10.snippetmanager.snippet.model.dto

import jakarta.validation.constraints.NotBlank

data class EditSnippetDTO(
    @NotBlank(message = "Name is required")
    val name: String,

    @NotBlank(message = "Name is required")
    val snippet: String,
)
