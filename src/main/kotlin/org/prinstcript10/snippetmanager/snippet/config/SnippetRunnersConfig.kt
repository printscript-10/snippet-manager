package org.prinstcript10.snippetmanager.snippet.config

import org.prinstcript10.snippetmanager.integration.runner.PrintscriptRunnerService
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SnippetRunnersConfig(
    private val printscriptRunnerService: PrintscriptRunnerService
) {

    @Bean
    fun SnippetRunnerMap(): Map<SnippetLanguage, RunnerService> {
        return mapOf(
            SnippetLanguage.PRINTSCRIPT to printscriptRunnerService
        )
    }

}