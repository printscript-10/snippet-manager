package org.prinstcript10.snippetmanager.redis.lint.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.slf4j.LoggerFactory
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

        private val logger = LoggerFactory.getLogger(LintRequestProducer::class.java)

        suspend fun publishEvent(event: String) {
            logger.info("Publishing lint request: $event")
            emit(event)
        }
    }
