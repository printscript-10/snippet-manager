package org.prinstcript10.snippetmanager.snippet.model.enum

enum class SnippetLanguage(private val extension: String) {
    PRINTSCRIPT("ps"),
    ;

    fun getExtension(): String {
        return extension
    }
}
