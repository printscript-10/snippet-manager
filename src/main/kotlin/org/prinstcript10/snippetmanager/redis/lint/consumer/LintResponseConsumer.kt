package org.prinstcript10.snippetmanager.redis.lint.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.austral.ingsis.redis.RedisStreamConsumer
import org.prinstcript10.snippetmanager.redis.lint.event.LintResponseEvent
import org.prinstcript10.snippetmanager.snippet.service.SnippetService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component

@Component
class LintResponseConsumer
    @Autowired
    constructor(
        @Value("\${lint_response}")
        private val streamName: String,
        @Value("\${linteando_ando}")
        private val groupName: String,
        private val objectMapper: ObjectMapper,
        private val snippetService: SnippetService,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamConsumer<String>(streamName, groupName, redis) {

        override fun onMessage(record: ObjectRecord<String, String>) {
            val lintResponse: LintResponseEvent = objectMapper.readValue(record.value)

            println("Received lint response: $lintResponse")
            snippetService.updateUserSnippetLintingStatus(
                lintResponse.snippetId,
                lintResponse.userId,
                lintResponse.status,
            )
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(java.time.Duration.ofMillis(10000))
                .targetType(String::class.java)
                .build()
        }
    }
