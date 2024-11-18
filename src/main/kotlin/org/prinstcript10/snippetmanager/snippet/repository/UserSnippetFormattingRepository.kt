package org.prinstcript10.snippetmanager.snippet.repository

import org.prinstcript10.snippetmanager.snippet.model.entity.UserSnippetFormatting
import org.springframework.data.jpa.repository.JpaRepository

interface UserSnippetFormattingRepository : JpaRepository<UserSnippetFormatting, String> {
    fun findAllByUserId(userId: String): List<UserSnippetFormatting>
    fun findFirstBySnippetIdAndUserId(snippetId: String, userId: String): UserSnippetFormatting?
}
