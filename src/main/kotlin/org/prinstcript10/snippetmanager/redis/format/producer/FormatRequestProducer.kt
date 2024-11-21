package org.prinstcript10.snippetmanager.redis.format.producer

import org.austral.ingsis.redis.RedisStreamProducer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class FormatRequestProducer
    @Autowired
    constructor(
        @Value("\${format_request}")
        private val streamName: String,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamProducer(streamName, redis) {

        private val logger = LoggerFactory.getLogger(FormatRequestProducer::class.java)

        suspend fun publishEvent(event: String) {
            logger.info("Publishing format request: $event")
            emit(event)
        }
    }
