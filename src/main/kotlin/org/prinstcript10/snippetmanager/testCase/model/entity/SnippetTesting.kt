package org.prinstcript10.snippetmanager.testCase.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel
import org.prinstcript10.snippetmanager.testCase.model.enum.TestStatus

@Entity
data class SnippetTesting(
    @Enumerated(EnumType.STRING)
    var status: TestStatus = TestStatus.PENDING,

    @ManyToOne
    @JoinColumn(name = "testId", nullable = false)
    val testCase: TestCase? = null,
) : BaseModel()
