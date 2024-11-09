package org.prinstcript10.snippetmanager.rules.service

import jakarta.transaction.Transactional
import org.prinstcript10.snippetmanager.rules.model.dto.AddUserRuleDTO
import org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO
import org.prinstcript10.snippetmanager.rules.model.entity.UserRule
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.repository.RuleRepository
import org.prinstcript10.snippetmanager.rules.repository.UserRuleRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RulesService
    @Autowired
    constructor(
        private val ruleRepository: RuleRepository,
        private val userRuleRepository: UserRuleRepository,
    ) {

        fun getRules(ruleType: RuleType, userId: String): List<GetRuleDTO> {
            return ruleRepository.findAllRulesWithUserValuesByUserAndType(userId, ruleType)
        }

        @Transactional
        fun updateUserRules(rules: List<AddUserRuleDTO>, ruleType: RuleType, userId: String) {
            rules.forEach { rule ->
                val userRule = userRuleRepository.findFirstByUserIdAndRuleId(userId, rule.ruleId)
                if (userRule != null) {
                    userRule.value = rule.value
                    userRule.isActive = rule.isActive
                    userRuleRepository.save(userRule)
                } else {
                    userRuleRepository.save(
                        UserRule(
                            userId = userId,
                            rule = ruleRepository.findById(rule.ruleId).get(),
                            value = rule.value,
                            isActive = rule.isActive,
                        ),
                    )
                }
            }

            // REQUEST CAMBIAR LINT/FORMAT
        }
    }
