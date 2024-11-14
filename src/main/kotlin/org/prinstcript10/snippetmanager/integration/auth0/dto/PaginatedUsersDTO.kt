package org.prinstcript10.snippetmanager.integration.auth0.dto

data class PaginatedUsersDTO(
    val users: List<GetUserDTO>,
    val total: Int,
)
