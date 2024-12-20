package org.prinstcript10.snippetmanager.snippet.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetFormatStatus

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["userId", "snippetId"])])
data class UserSnippetFormatting(
    val userId: String = "",

    @Enumerated(EnumType.STRING)
    var status: SnippetFormatStatus = SnippetFormatStatus.PENDING,

    @ManyToOne
    @JoinColumn(name = "snippetId", nullable = false)
    val snippet: Snippet? = null,
) : BaseModel()
