package org.prinstcript10.snippetmanager.redis.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class LintRequestProducer
    @Autowired
    constructor(
        @Value("\${lint_request}")
        private val streamName: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamName, redis) {

        suspend fun publishEvent(event: String) {
            emit(event)
        }
    }
