package org.prinstcript10.snippetmanager.integration.runner

import org.prinstcript10.snippetmanager.integration.runner.dto.RunSnippetResponseDTO
import org.springframework.http.ResponseEntity

interface RunnerService {
    fun validateSnippet(snippet: String, token: String): ResponseEntity<Any>

    fun runSnippet(inputs: List<String>, snippetId: String, token: String): ResponseEntity<RunSnippetResponseDTO>
}
