package org.prinstcript10.snippetmanager.rules.controller

import org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.service.RulesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rules")
@Validated
class RulesController(
    @Autowired
    private val rulesService: RulesService,
) {

//    @PostMapping
//    fun addRule(
//        @Valid @RequestBody rules: List<AddUserRuleDTO>,
//        @AuthenticationPrincipal jwt: Jwt,
//    ) {
//        return rulesService.updateUserRules(rules, jwt.subject)
//    }

    @GetMapping("/{ruleType}")
    fun getRule(
        @PathVariable ruleType: RuleType,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<GetRuleDTO> {
        return rulesService.getRules(ruleType, jwt.subject)
    }
}
