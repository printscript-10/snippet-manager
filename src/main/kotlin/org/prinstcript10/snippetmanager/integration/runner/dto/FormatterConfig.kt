package org.prinstcript10.snippetmanager.integration.runner.dto

data class FormatterConfig(
    var declaration_colon_trailing_whitespaces: Boolean? = null,
    var declaration_colon_leading_whitespaces: Boolean? = null,
    var assignation_equal_wrap_whitespaces: Boolean? = null,
    var println_trailing_line_jump: Int? = null,
    var if_block_indent_spaces: Int? = null,
)
