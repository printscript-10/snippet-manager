package org.prinstcript10.snippetmanager.redis.event

data class LintRequestEvent(
    val snippetId: String,
    val userId: String,
    val config: LintConfig,
)