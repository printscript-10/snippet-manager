package org.prinstcript10.snippetmanager.integration.permission

import org.prinstcript10.snippetmanager.snippet.model.dto.ShareSnippetDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.postForEntity

@Component
class PermissionService
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${permisiones_url}")
        private val permissionUrl: String,
    ) {

        fun createPermission(snippetId: String, token: String): ResponseEntity<Any> {
            try {
                val request = HttpEntity(null, getHeaders(token))
                return rest.postForEntity("$permissionUrl/$snippetId", request, Any::class.java)
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(e.message)
            }
        }

        fun shareSnippet(shareSnippetDTO: ShareSnippetDTO, token: String): ResponseEntity<Any> {
            try {
                val request = HttpEntity(shareSnippetDTO, getHeaders(token))
                return rest.postForEntity("$permissionUrl/share", request, Any::class.java)
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(e.message)
            }
        }

        fun getPermission(snippetId: String, token: String): ResponseEntity<Any> {
            try {
                val request = HttpEntity(null, getHeaders(token))
                return rest.exchange("$permissionUrl/$snippetId", HttpMethod.GET, request, Any::class.java)
            } catch (e: Exception) {
                return ResponseEntity.badRequest().body(e.message)
            }
        }

        fun getAllSnippetPermissions(token: String): ResponseEntity<List<SnippetPermissionDTO>> {
            return try {
                val request = HttpEntity(null, getHeaders(token))
                rest.exchange(
                    permissionUrl,
                    HttpMethod.GET,
                    request,
                    object : ParameterizedTypeReference<List<SnippetPermissionDTO>>() {},
                )
            } catch (e: Exception) {
                ResponseEntity.badRequest().body(null)
            }
        }

        fun deleteSnippetPermissions(snippetId: String, token: String): ResponseEntity<Any> {
            try {
                val request = HttpEntity(null, getHeaders(token))
                return rest.exchange("$permissionUrl/$snippetId", HttpMethod.DELETE, request, Any::class.java)
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
