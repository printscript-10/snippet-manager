package org.prinstcript10.snippetmanager.snippet.repository

import org.prinstcript10.snippetmanager.snippet.model.entity.Snippet
import org.springframework.data.jpa.repository.JpaRepository

interface SnippetRepository : JpaRepository<Snippet, String>
