package org.prinstcript10.snippetmanager.snippet.model.dto

import jakarta.validation.constraints.NotBlank
import org.prinstcript10.snippetmanager.shared.enumValidator.EnumFormat
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage

data class SnippetDTO(
    val id: String,
    @NotBlank(message = "Name is required")
    val name: String,

    @EnumFormat(enumClass = SnippetLanguage::class, message = "Invalid language")
    val language: SnippetLanguage,

    val content: String?,

    val extension: String,
)
