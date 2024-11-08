package org.prinstcript10.snippetmanager.snippet.repository

import org.prinstcript10.snippetmanager.testCase.model.entity.TestCase
import org.springframework.data.jpa.repository.JpaRepository
interface TestCaseRepository : JpaRepository<TestCase, String> {
    fun findBySnippetId(snippetId: String): List<TestCase>
}
