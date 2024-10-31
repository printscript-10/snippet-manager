package org.prinstcript10.snippetmanager.snippet.repository

import org.prinstcript10.snippetmanager.snippet.model.entity.Snippet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface SnippetRepository : JpaRepository<Snippet, String> {
    @Query(
        value = """
            SELECT * FROM snippet
            WHERE id IN (:snippetIds)
            AND (LOWER(name) LIKE CONCAT('%', LOWER(:param), '%')
            OR LOWER(language) LIKE CONCAT('%', LOWER(:param), '%'))
            LIMIT :limit OFFSET :offset
        """,
        nativeQuery = true,
    )
    fun findAll(snippetIds: List<String>, limit: Int, offset: Int, param: String): List<Snippet>
}
