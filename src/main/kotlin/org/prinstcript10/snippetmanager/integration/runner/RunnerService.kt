package org.prinstcript10.snippetmanager.integration.runner

import org.springframework.http.ResponseEntity

interface RunnerService {
    fun validateSnippet(snippet: String, token: String): ResponseEntity<Any>
}
