package org.prinstcript10.snippetmanager.redis.consumer

import org.austral.ingsis.redis.RedisStreamConsumer
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
        redis: RedisTemplate<String, String>,
    ) : RedisStreamConsumer<String>(streamName, groupName, redis) {

        override fun onMessage(record: ObjectRecord<String, String>) {
            TODO("Not yet implemented")
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(java.time.Duration.ofMillis(10000))
                .targetType(String::class.java)
                .build()
        }
    }
