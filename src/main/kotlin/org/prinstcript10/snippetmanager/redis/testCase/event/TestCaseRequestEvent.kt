package org.prinstcript10.snippetmanager.redis.testCase.event

data class TestCaseRequestEvent(
    val testCaseId: String,
    val snippetId: String,
    val inputs: List<String>,
)
