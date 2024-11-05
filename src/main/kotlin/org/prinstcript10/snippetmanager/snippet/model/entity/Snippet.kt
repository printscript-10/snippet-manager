package org.prinstcript10.snippetmanager.snippet.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.OneToMany
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage
import org.prinstcript10.snippetmanager.testCase.model.entity.TestCase

@Entity
data class Snippet(
    var name: String = "",

    @Enumerated(EnumType.STRING)
    val language: SnippetLanguage = SnippetLanguage.PRINTSCRIPT,

    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "snippet")
    val testCases: List<TestCase> = listOf(),
) : BaseModel()
