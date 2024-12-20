package org.prinstcript10.snippetmanager.snippet.controller

import jakarta.validation.Valid
import org.prinstcript10.snippetmanager.integration.auth0.dto.PaginatedUsersDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.CreateSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.EditSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.GetSnippetLanguageDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.PaginatedSnippetsDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.ShareSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.SnippetDTO
import org.prinstcript10.snippetmanager.snippet.service.SnippetService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("snippets")
@Validated
class SnippetController(
    @Autowired
    private val snippetService: SnippetService,
) {
    private val logger = LoggerFactory.getLogger(SnippetService::class.java)

    @GetMapping("friends")
    fun getSnippetFriends(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("pageSize", defaultValue = "10") pageSize: Int,
        @RequestParam("param", defaultValue = "") param: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): PaginatedUsersDTO {
        return snippetService.getSnippetFriends(page, pageSize, param, jwt.subject)
    }

    @GetMapping("{snippetId}")
    fun getSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): SnippetDTO {
        return snippetService.getSnippet(snippetId, jwt.tokenValue)
    }

    @PostMapping
    suspend fun createSnippet(
        @Valid @RequestBody createSnippetDTO: CreateSnippetDTO,
        @AuthenticationPrincipal jwt: Jwt,
    ): SnippetDTO {
        return snippetService.createSnippet(createSnippetDTO, jwt.subject, jwt.tokenValue)
    }

    @GetMapping()
    fun getAllSnippets(
        @RequestParam("page", defaultValue = "0") page: Int,
        @RequestParam("pageSize", defaultValue = "10") pageSize: Int,
        @RequestParam("param", defaultValue = "") param: String,
        @AuthenticationPrincipal jwt: Jwt,
    ): PaginatedSnippetsDTO {
        return snippetService.getAllSnippets(jwt.tokenValue, page, pageSize, param, jwt.subject)
    }

    @GetMapping("languages")
    fun getSnippetLanguages(): List<GetSnippetLanguageDTO> {
        return snippetService.getSnippetLanguages()
    }

    @PutMapping("{snippetId}")
    suspend fun updateSnippet(
        @PathVariable("snippetId") snippetId: String,
        @Valid @RequestBody editSnippetDTO: EditSnippetDTO,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        return snippetService.updateSnippet(editSnippetDTO, snippetId, jwt.tokenValue)
    }

    @DeleteMapping("{snippetId}")
    fun deleteSnippet(
        @PathVariable("snippetId") snippetId: String,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        return snippetService.deleteSnippet(snippetId, jwt.tokenValue)
    }

    @PostMapping("share")
    suspend fun shareSnippet(
        @Valid @RequestBody shareSnippetDTO: ShareSnippetDTO,
        @AuthenticationPrincipal jwt: Jwt,
    ) {
        return snippetService.shareSnippet(shareSnippetDTO, jwt.tokenValue)
    }

    @GetMapping("elexplotes")
    fun explode() {
        logger.error("El explotes exploto")
    }
}
