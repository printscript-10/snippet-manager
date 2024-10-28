package org.prinstcript10.snippetmanager.snippet.controller

import jakarta.validation.Valid
import org.prinstcript10.snippetmanager.snippet.model.dto.CreateSnippetDTO
import org.prinstcript10.snippetmanager.snippet.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("snippets")
@Validated
class SnippetController(
    @Autowired
    private val snippetService: SnippetService
) {

    @PostMapping
    fun createSnippet(
        @Valid @RequestBody createSnippetDTO: CreateSnippetDTO,
        @AuthenticationPrincipal jwt: Jwt
    ) {
        return snippetService.createSnippet(createSnippetDTO, jwt.subject, jwt.tokenValue)
    }

    @GetMapping("{snippetId}")
    fun getSnippet(
        @PathVariable("snippetId") snippetId: String
    ): String {
        return snippetService.getSnippet(snippetId)
    }

}
