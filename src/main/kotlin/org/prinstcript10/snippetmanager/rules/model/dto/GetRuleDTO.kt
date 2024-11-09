package org.prinstcript10.snippetmanager.rules.model.dto

import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.model.enum.ValueType

data class GetRuleDTO(
    val id: String,
    val name: String,
    val value: String?,
    val active: Boolean? = false,
    val valueType: ValueType,
    val ruleType: RuleType,
)
