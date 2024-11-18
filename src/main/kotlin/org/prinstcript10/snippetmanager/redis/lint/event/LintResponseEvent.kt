package org.prinstcript10.snippetmanager.redis.lint.event

import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLintingStatus

data class LintResponseEvent(
    val snippetId: String,
    val userId: String,
    val status: SnippetLintingStatus,
)
