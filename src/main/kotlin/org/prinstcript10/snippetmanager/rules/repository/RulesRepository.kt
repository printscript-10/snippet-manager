package org.prinstcript10.snippetmanager.rules.repository

import org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO
import org.prinstcript10.snippetmanager.rules.model.entity.Rule
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RulesRepository : JpaRepository<Rule, String> {
    @Query(
        """
        SELECT new org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO(
            r.id,
            r.name,
            COALESCE(ur.value, NULL),
            COALESCE(ur.isActive, false),
            r.valueType,
            r.type
        )
        FROM Rule r
        LEFT JOIN r.userRules ur ON r.id = ur.rule.id AND ur.userId = :userId
        WHERE r.type = :ruleType
    """,
        nativeQuery = false,
    )
    fun findAllRulesWithUserValuesByUserAndType(userId: String, ruleType: RuleType): List<GetRuleDTO>
}
