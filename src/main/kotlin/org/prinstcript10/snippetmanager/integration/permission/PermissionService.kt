package org.prinstcript10.snippetmanager.integration.permission

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PermissionService
    @Autowired
    constructor(
        private val rest: RestTemplate,
        @Value("\${permisiones_url}")
        private val runnerUrl: String,
){

    fun createPermission(

    ) {

    }

    private fun getHeaders(token: String): HttpHeaders {
        return HttpHeaders().apply {
            set("Authorization", "Bearer $token")
        }
    }

}
