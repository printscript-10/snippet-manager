package org.prinstcript10.snippetmanager.integration.permission

data class SnippetPermissionDTO(
    val snippetId: String,
    val userId: String,
    val ownership: SnippetOwnership,
)
