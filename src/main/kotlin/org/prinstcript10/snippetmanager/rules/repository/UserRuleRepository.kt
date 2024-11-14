package org.prinstcript10.snippetmanager.rules.repository

import org.prinstcript10.snippetmanager.rules.model.entity.UserRule
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.springframework.data.jpa.repository.JpaRepository

interface UserRuleRepository : JpaRepository<UserRule, String> {
    fun findFirstByUserIdAndRuleId(userId: String, ruleId: String): UserRule?
    fun findAllByUserIdAndRuleType(userId: String, ruleType: RuleType): List<UserRule>
}
