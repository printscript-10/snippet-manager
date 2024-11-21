package org.prinstcript10.snippetmanager.redis.testCase.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.austral.ingsis.redis.RedisStreamConsumer
import org.prinstcript10.snippetmanager.redis.testCase.event.TestCaseResponseEvent
import org.prinstcript10.snippetmanager.testCase.model.enum.TestStatus
import org.prinstcript10.snippetmanager.testCase.service.TestCaseService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component

@Component
class TestCaseResponseConsumer
    @Autowired
    constructor(
        @Value("\${test_response}")
        private val streamName: String,
        @Value("\${testando_ando}")
        private val groupName: String,
        private val objectMapper: ObjectMapper,
        private val testCaseService: TestCaseService,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamConsumer<String>(streamName, groupName, redis) {

        override fun onMessage(record: ObjectRecord<String, String>) {
            val testResponse: TestCaseResponseEvent = objectMapper.readValue(record.value)
            val testCase = testCaseService.getTestCaseById(testResponse.testCaseId)

            val status = if (testCase.output.toList() == testResponse.outputs.toList() &&
                testResponse.errors.isNullOrEmpty()
            ) {
                TestStatus.SUCCESS
            } else {
                TestStatus.FAIL
            }

            println("Received test case response: $testResponse")
            testCaseService.updateSnippetTestCaseStatus(
                testResponse.testCaseId,
                status,
            )
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(java.time.Duration.ofMillis(50000))
                .targetType(String::class.java)
                .build()
        }
    }
