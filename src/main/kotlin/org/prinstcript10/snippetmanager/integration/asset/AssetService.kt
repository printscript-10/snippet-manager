package org.prinstcript10.snippetmanager.integration.asset

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AssetService
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${balde_url}")
        private val bucketUrl: String,
    ) {

    fun saveSnippet(
        snippetId: String,
        snippet: String
    ): ResponseEntity<String> {
        try {
            val request = HttpEntity(snippet, HttpHeaders())
            rest.put("$bucketUrl/$snippetId", request)
            return ResponseEntity.ok(null)
        } catch (e: Exception) {
            return ResponseEntity.badRequest().build()
        }
    }

    fun getSnippet(
        snippetId: String
    ): String {
        return rest.getForEntity("$bucketUrl/$snippetId", String::class.java).body!!
    }

}