package org.prinstcript10.snippetmanager.rules.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.model.enum.ValueType
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel

@Entity
data class Rule(
    val name: String = "",

    @Enumerated(EnumType.STRING)
    val type: RuleType = RuleType.LINT,

    @Enumerated(EnumType.STRING)
    val valueType: ValueType = ValueType.STRING,

    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "rule")
    val userRules: List<UserRule> = listOf(),
) : BaseModel()
