package org.prinstcript10.snippetmanager.snippet.repository

import org.prinstcript10.snippetmanager.snippet.model.entity.UserSnippetLinting
import org.springframework.data.jpa.repository.JpaRepository

interface UserSnippetLintingRepository : JpaRepository<UserSnippetLinting, String> {
    fun findAllByUserId(userId: String): List<UserSnippetLinting>
    fun findFirstBySnippetIdAndUserId(snippetId: String, userId: String): UserSnippetLinting?
}
