package org.prinstcript10.snippetmanager.redis.testCase.event

data class TestCaseResponseEvent(
    val testCaseId: String,
    val outputs: List<String>,
    val errors: List<String>?,
)
