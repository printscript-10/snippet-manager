package org.prinstcript10.snippetmanager.rules.service

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.prinstcript10.snippetmanager.integration.runner.PrintscriptRunnerService
import org.prinstcript10.snippetmanager.integration.runner.dto.FormatSnippetResponseDTO
import org.prinstcript10.snippetmanager.integration.runner.dto.FormatterConfig
import org.prinstcript10.snippetmanager.redis.format.event.FormatRequestEvent
import org.prinstcript10.snippetmanager.redis.format.producer.FormatRequestProducer
import org.prinstcript10.snippetmanager.redis.lint.event.LintConfig
import org.prinstcript10.snippetmanager.redis.lint.event.LintRequestEvent
import org.prinstcript10.snippetmanager.redis.lint.producer.LintRequestProducer
import org.prinstcript10.snippetmanager.rules.model.dto.AddUserRuleDTO
import org.prinstcript10.snippetmanager.rules.model.dto.GetRuleDTO
import org.prinstcript10.snippetmanager.rules.model.entity.UserRule
import org.prinstcript10.snippetmanager.rules.model.enum.RuleType
import org.prinstcript10.snippetmanager.rules.repository.RuleRepository
import org.prinstcript10.snippetmanager.rules.repository.UserRuleRepository
import org.prinstcript10.snippetmanager.snippet.service.SnippetService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service

@Service
class RulesService
    @Autowired
    constructor(
        private val snippetService: SnippetService,
        private val ruleRepository: RuleRepository,
        private val userRuleRepository: UserRuleRepository,
        private val lintRequestProducer: LintRequestProducer,
        private val objectMapper: ObjectMapper,
        private val runnerService: PrintscriptRunnerService,
        private val formatRequestProducer: FormatRequestProducer,
    ) {

        private val logger = LoggerFactory.getLogger(RulesService::class.java)

        fun getRules(ruleType: RuleType, userId: String): List<GetRuleDTO> {
            logger.info("Getting rules for $ruleType and user $userId")
            return ruleRepository.findAllRulesWithUserValuesByUserAndType(userId, ruleType)
        }

        @Transactional
        suspend fun updateUserRules(rules: List<AddUserRuleDTO>, ruleType: RuleType, userId: String) {
            logger.info("Updating user rules for user: $userId")
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
            if (ruleType == RuleType.LINT) {
                val snippetIds: List<String> = snippetService.resetUserSnippetLinting(userId)
                publishLintEvents(userId, snippetIds)
            } else if (ruleType == RuleType.FORMAT) {
                val snippetIds: List<String> = snippetService.resetUserSnippetFormatting(userId)
                publishFormatEvents(userId, snippetIds)
            }
        }

        fun formatSnippet(snippet: String, token: String, userId: String): ResponseEntity<FormatSnippetResponseDTO> {
            logger.info("Parsing format rules for user: $userId")
            val config = parseFormatRules(userId)
            logger.info("Formatting snippet: $snippet")
            return runnerService.formatSnippet(snippet, config, token)
        }

        suspend fun publishFormatEvents(userId: String, snippetIds: List<String>) {
            val config = parseFormatRules(userId)
            snippetIds.forEach {
                formatRequestProducer.publishEvent(
                    objectMapper.writeValueAsString(
                        FormatRequestEvent(
                            userId = userId,
                            snippetId = it,
                            config = config,
                        ),
                    ),
                )
            }
        }

        suspend fun publishLintEvents(userId: String, snippetIds: List<String>) {
            snippetIds.forEach {
                lintRequestProducer.publishEvent(
                    objectMapper.writeValueAsString(
                        LintRequestEvent(
                            snippetId = it,
                            userId = userId,
                            config = parseLintRules(userId),
                        ),
                    ),
                )
            }
        }

        fun parseLintRules(userId: String): LintConfig {
            logger.info("Parsing linting rules for user: $userId")
            val rules: List<UserRule> = userRuleRepository.findAllByUserIdAndRuleType(userId, RuleType.LINT)
            val config = LintConfig(null, null, null)

            rules.forEach {
                if (it.isActive) {
                    when (it.rule!!.name) {
                        "allow_expression_in_println" -> config.allow_expression_in_println = it.value.toBoolean()
                        "allow_expression_in_readinput" -> config.allow_expression_in_readinput = it.value.toBoolean()
                        "naming_convention" -> config.naming_convention = it.value
                    }
                }
            }
            return config
        }

        fun parseFormatRules(userId: String): FormatterConfig {
            val formatRules: List<UserRule> = userRuleRepository.findAllByUserIdAndRuleType(userId, RuleType.FORMAT)
            val formatConfig = FormatterConfig(null, null, null, null, null)

            formatRules.forEach {
                if (it.isActive) {
                    when (it.rule!!.name) {
                        "declaration_colon_trailing_whitespaces" ->
                            formatConfig.declaration_colon_trailing_whitespaces =
                                it.value.toBoolean()
                        "declaration_colon_leading_whitespaces" ->
                            formatConfig.declaration_colon_leading_whitespaces =
                                it.value.toBoolean()
                        "assignation_equal_wrap_whitespaces" ->
                            formatConfig.assignation_equal_wrap_whitespaces =
                                it.value.toBoolean()
                        "println_trailing_line_jump" ->
                            formatConfig.println_trailing_line_jump =
                                it.value.toInt()
                        "if_block_indent_spaces" ->
                            formatConfig.if_block_indent_spaces =
                                it.value.toInt()
                    }
                }
            }

            return formatConfig
        }
    }
