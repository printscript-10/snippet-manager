package org.prinstcript10.snippetmanager.integration.auth0

import org.prinstcript10.snippetmanager.integration.auth0.dto.GetUserDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

@Component
class Auth0Service
    @Autowired
    constructor(
        @Value("\${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private val auth0URL: String,
        @Value("\${auth0.token}")
        private val token: String,
        private val rest: RestTemplate,
    ) {

        fun getUsers(
            page: Int,
            perPage: Int,
            nickname: String,
        ): ResponseEntity<List<GetUserDTO>> {
            try {
                val request: HttpEntity<Void> = HttpEntity(getHeaders())
                return rest.exchange(
                    "$auth0URL/api/v2/users?" +
                        "filter=user_id,nickname" +
                        "&per_page=$perPage" +
                        "&page=$page" +
                        "&q=nickname:*$nickname*",
                    HttpMethod.GET,
                    request,
                    object : ParameterizedTypeReference<List<GetUserDTO>>() {},
                )
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        fun getUserById(userId: String): ResponseEntity<GetUserDTO> {
            try {
                val request: HttpEntity<Void> = HttpEntity(getHeaders())
                return rest.exchange(
                    "$auth0URL/api/v2/users/$userId",
                    HttpMethod.GET,
                    request,
                    GetUserDTO::class.java,
                )
            } catch (e: HttpClientErrorException) {
                throw e
            }
        }

        private fun getHeaders(): HttpHeaders {
            return HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                set("Authorization", "Bearer $token")
            }
        }
    }
