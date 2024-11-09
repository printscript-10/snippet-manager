package org.prinstcript10.snippetmanager.rules.model.entity

import jakarta.persistence.Entity
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import org.prinstcript10.snippetmanager.shared.baseModel.BaseModel

@Entity
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["userId", "ruleId"])])
data class UserRule(
    val userId: String = "",

    var value: String = "",

    var isActive: Boolean = false,

    @ManyToOne
    @JoinColumn(name = "ruleId", referencedColumnName = "id", nullable = false)
    val rule: Rule? = null,
) : BaseModel()
