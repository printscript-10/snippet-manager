package org.prinstcript10.snippetmanager.integration.permission

import org.prinstcript10.snippetmanager.integration.permission.dto.SnippetPermissionDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.ShareSnippetDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

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
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(mapOf("error" to "Failed to create permission", "message" to e.message))
            }
        }

        fun shareSnippet(shareSnippetDTO: ShareSnippetDTO, token: String): ResponseEntity<Any> {
            try {
                val request = HttpEntity(shareSnippetDTO, getHeaders(token))
                return rest.postForEntity("$permissionUrl/share", request, Any::class.java)
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        fun getPermission(snippetId: String, token: String): ResponseEntity<SnippetPermissionDTO> {
            return try {
                val request = HttpEntity(null, getHeaders(token))
                rest.exchange("$permissionUrl/$snippetId", HttpMethod.GET, request, SnippetPermissionDTO::class.java)
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        fun getSnippetOwner(snippetId: String, token: String): ResponseEntity<SnippetPermissionDTO> {
            return try {
                val request = HttpEntity(null, getHeaders(token))
                rest.exchange(
                    "$permissionUrl/owner/$snippetId",
                    HttpMethod.GET,
                    request,
                    SnippetPermissionDTO::class.java,
                )
            } catch (e: HttpClientErrorException) {
                throw e
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
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        fun deleteSnippetPermissions(snippetId: String, token: String): ResponseEntity<Any> {
            try {
                val request = HttpEntity(null, getHeaders(token))
                return rest.exchange("$permissionUrl/$snippetId", HttpMethod.DELETE, request, Any::class.java)
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
