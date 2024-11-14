package org.prinstcript10.snippetmanager.rules.controller

import jakarta.validation.Valid
import org.prinstcript10.snippetmanager.integration.runner.dto.FormatSnippetResponseDTO
import org.prinstcript10.snippetmanager.rules.model.dto.AddUserRuleDTO
import org.prinstcript10.snippetmanager.rules.model.dto.FormatSnippetRequestDTO
import org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.service.RulesService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("rules")
@Validated
class RulesController(
    @Autowired
    private val rulesService: RulesService,
) {

    @PostMapping("/{ruleType}")
    suspend fun addRules(
        @Valid @RequestBody rules: List<AddUserRuleDTO>,
        @PathVariable ruleType: RuleType,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        return rulesService.updateUserRules(rules, ruleType, jwt.subject)
    }

    @GetMapping("/{ruleType}")
    fun getRule(
        @PathVariable ruleType: RuleType,
        @AuthenticationPrincipal jwt: Jwt,
    ): List<GetRuleDTO> {
        return rulesService.getRules(ruleType, jwt.subject)
    }

    @PostMapping("format")
    fun formatSnippet(
        @Valid @RequestBody snippet: FormatSnippetRequestDTO,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<FormatSnippetResponseDTO> {
        return rulesService.formatSnippet(snippet.snippet, jwt.tokenValue, jwt.subject)
    }
}
