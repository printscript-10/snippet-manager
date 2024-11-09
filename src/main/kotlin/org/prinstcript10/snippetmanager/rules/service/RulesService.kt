package org.prinstcript10.snippetmanager.rules.service

import org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.repository.RulesRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class RulesService(
    @Autowired
    private val rulesRepository: RulesRepository,
) {

    fun getRules(ruleType: RuleType, userId: String): List<GetRuleDTO> {
        return rulesRepository.findAllRulesWithUserValuesByUserAndType(userId, ruleType)
    }

//    fun updateUserRules(rules: List<AddUserRuleDTO>, userId: String) {
//
//    }
}
