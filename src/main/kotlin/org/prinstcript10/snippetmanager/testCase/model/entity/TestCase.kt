package org.prinstcript10.snippetmanager.testCase.model.entity

import jakarta.persistence.CascadeType
import jakarta.persistence.CollectionTable
import jakarta.persistence.Column
import jakarta.persistence.ElementCollection
import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.OneToMany
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel
import org.prinstcript10.snippetmanager.snippet.model.entity.Snippet

@Entity
data class TestCase(
    val name: String = "",

    @ElementCollection
    @CollectionTable(name = "test_input", joinColumns = [JoinColumn(name = "test_id")])
    @Column(name = "input")
    val input: List<String> = listOf(),

    @ElementCollection
    @CollectionTable(name = "test_output", joinColumns = [JoinColumn(name = "test_id")])
    @Column(name = "output")
    val output: List<String> = listOf(),

    @ManyToOne
    @JoinColumn(name = "snippetId", referencedColumnName = "id", nullable = false)
    val snippet: Snippet? = null,

    @OneToMany(cascade = [CascadeType.REMOVE], mappedBy = "testCase")
    val snippetTesting: List<SnippetTesting> = listOf(),
) : BaseModel()
