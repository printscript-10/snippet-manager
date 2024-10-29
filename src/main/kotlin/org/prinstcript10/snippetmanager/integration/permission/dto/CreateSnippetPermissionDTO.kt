package org.prinstcript10.snippetmanager.integration.permission.dto

import jakarta.validation.constraints.NotBlank
import org.prinstcript10.snippetmanager.integration.permission.SnippetOwnership
import org.prinstcript10.snippetmanager.shared.enumValidator.EnumFormat

data class CreateSnippetPermissionDTO(
    @NotBlank(message = "snippetId is required")
    val snippetId: String,

    @EnumFormat(enumClass = SnippetOwnership::class, message = "Invalid ownership")
    val ownership: SnippetOwnership,
)
