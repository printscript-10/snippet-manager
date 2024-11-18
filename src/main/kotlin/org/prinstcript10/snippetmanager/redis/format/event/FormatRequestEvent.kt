package org.prinstcript10.snippetmanager.redis.format.event

import org.prinstcript10.snippetmanager.integration.runner.dto.FormatterConfig

data class FormatRequestEvent(
    val userId: String,
    val snippetId: String,
    val config: FormatterConfig,
)
