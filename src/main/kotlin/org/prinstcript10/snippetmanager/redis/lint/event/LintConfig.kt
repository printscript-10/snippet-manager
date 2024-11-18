package org.prinstcript10.snippetmanager.redis.lint.event

data class LintConfig(
    var allow_expression_in_println: Boolean?,
    var allow_expression_in_readinput: Boolean?,
    var naming_convention: String?,
)
