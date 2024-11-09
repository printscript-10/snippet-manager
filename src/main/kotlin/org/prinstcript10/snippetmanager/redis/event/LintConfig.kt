package org.prinstcript10.snippetmanager.redis.event

data class LintConfig(
    val allow_expression_in_println: Boolean?,
    val allow_expression_in_readinput: Boolean?,
    val naming_convention: String?,
)
