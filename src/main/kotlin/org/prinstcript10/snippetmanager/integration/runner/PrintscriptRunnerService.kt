package org.prinstcript10.snippetmanager.integration.runner

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PrintscriptRunnerService
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${printscript_runner_url}")
        private val runnerUrl: String,
    ) : RunnerService {

        override fun validateSnippet(
            snippet: String,
            token: String,
        ): ResponseEntity<Any> {
            try {
                val request = HttpEntity(ValidateSnippetDTO(snippet), getHeaders(token))
                return rest.exchange("$runnerUrl/validate", HttpMethod.PUT, request, Any::class.java)
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(e.message)
            }
        }

        private fun getHeaders(token: String): HttpHeaders {
            return HttpHeaders().apply {
                set("Authorization", "Bearer $token")
            }
        }
    }
