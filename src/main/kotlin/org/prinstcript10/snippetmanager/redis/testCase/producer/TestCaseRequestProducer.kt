package org.prinstcript10.snippetmanager.redis.testCase.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class TestCaseRequestProducer
    @Autowired
    constructor(
        @Value("\${test_request}")
        private val streamName: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamName, redis) {

        suspend fun publishEvent(event: String) {
            println("Publishing test case request: $event")
            emit(event)
        }
    }
