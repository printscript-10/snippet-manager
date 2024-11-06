package org.prinstcript10.snippetmanager.integration.runner

import org.prinstcript10.snippetmanager.integration.runner.dto.RunSnippetDTO
import org.prinstcript10.snippetmanager.integration.runner.dto.RunSnippetResponseDTO
import org.prinstcript10.snippetmanager.integration.runner.dto.ValidateSnippetDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
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
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        override fun runSnippet(
            inputs: List<String>,
            snippetId: String,
            token: String,
        ): ResponseEntity<RunSnippetResponseDTO> {
            try {
                val runSnippetDTO = RunSnippetDTO(inputs)
                val request = HttpEntity(runSnippetDTO, getHeaders(token))
                return rest.exchange(
                    "$runnerUrl/run/$snippetId",
                    HttpMethod.POST,
                    request,
                    RunSnippetResponseDTO::class.java,
                )
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        private fun getHeaders(token: String): HttpHeaders {
            return HttpHeaders().apply {
                set("Authorization", "Bearer $token")
            }
        }
    }
