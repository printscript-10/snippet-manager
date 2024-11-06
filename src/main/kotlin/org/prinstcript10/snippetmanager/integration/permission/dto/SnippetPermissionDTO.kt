package org.prinstcript10.snippetmanager.integration.permission.dto

import org.prinstcript10.snippetmanager.integration.permission.SnippetOwnership

data class SnippetPermissionDTO(
    val snippetId: String,
    val userId: String,
    val ownership: SnippetOwnership,
)
