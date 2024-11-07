package org.prinstcript10.snippetmanager.snippet.model.dto

data class PaginatedSnippetDTO(
    val id: String,
    val name: String,
    val language: String,
    val extension: String,
    val compliance: String,
    val author: String,
)
