package org.prinstcript10.snippetmanager.snippet.model.dto

import jakarta.validation.constraints.NotBlank

data class ShareSnippetDTO(
    @NotBlank(message = "SnippetId is required")
    val snippetId: String,

    @NotBlank(message = "UserId is required")
    val userId: String,
)
