package org.prinstcript10.snippetmanager.redis.format.event

import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetFormatStatus

data class FormatResponseEvent(
    val snippetId: String,
    val userId: String,
    val status: SnippetFormatStatus,
    val formattedSnippet: String?,
)
