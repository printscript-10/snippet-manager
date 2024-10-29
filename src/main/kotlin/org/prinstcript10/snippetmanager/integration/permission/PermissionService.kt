package org.prinstcript10.snippetmanager.integration.permission

import org.prinstcript10.snippetmanager.integration.permission.dto.CreateSnippetPermissionDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PermissionService
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${permisiones_url}")
        private val permissionUrl: String,
    ) {

        fun createPermission(snippetId: String, ownership: SnippetOwnership, token: String): ResponseEntity<String> {
            try {
                val createSnippetPermissionDTO = CreateSnippetPermissionDTO(snippetId, ownership)
                val request = HttpEntity(createSnippetPermissionDTO, getHeaders(token))
                rest.postForEntity("$permissionUrl", request, String::class.java)
                return ResponseEntity.ok(null)
            } catch (e: Exception) {
                println(e.message)
                return ResponseEntity.badRequest().build()
            }
        }

        fun getPermission(snippetId: String, token: String): ResponseEntity<String> {
            try {
                val request = HttpEntity(null, getHeaders(token))
                val res = rest.exchange("$permissionUrl/$snippetId", HttpMethod.GET, request, String::class.java)
                return ResponseEntity.ok(res.body)
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
