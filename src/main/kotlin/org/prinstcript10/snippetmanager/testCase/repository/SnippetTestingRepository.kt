package org.prinstcript10.snippetmanager.snippet.repository

import org.prinstcript10.snippetmanager.testCase.model.entity.SnippetTesting
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface SnippetTestingRepository : JpaRepository<SnippetTesting, String> {
    @Query(
        """
        SELECT st FROM SnippetTesting st
        JOIN st.testCase tc
        WHERE tc.snippet.id = :snippetId
    """,
    )
    fun findAllBySnippetId(
        @Param("snippetId") snippetId: String,
    ): List<SnippetTesting>
    fun findFirstByTestCaseId(testCaseId: String): SnippetTesting?
}
