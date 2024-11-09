package org.prinstcript10.snippetmanager.rules.repository

import org.prinstcript10.snippetmanager.rules.model.entity.UserRule
import org.springframework.data.jpa.repository.JpaRepository

interface UserRuleRepository : JpaRepository<UserRule, String> {
    fun findFirstByUserIdAndRuleId(userId: String, ruleId: String): UserRule?
}
