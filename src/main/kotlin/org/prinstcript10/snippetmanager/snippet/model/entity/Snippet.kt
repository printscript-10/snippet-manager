package org.prinstcript10.snippetmanager.snippet.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage

@Entity
data class Snippet(
    val name: String,

    @Enumerated(EnumType.STRING)
    val language: SnippetLanguage,
) : BaseModel() {
    constructor() : this("", SnippetLanguage.PRINTSCRIPT)
}
