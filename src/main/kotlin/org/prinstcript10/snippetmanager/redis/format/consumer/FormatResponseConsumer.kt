package org.prinstcript10.snippetmanager.redis.format.consumer

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.austral.ingsis.redis.RedisStreamConsumer
import org.prinstcript10.snippetmanager.integration.asset.AssetService
import org.prinstcript10.snippetmanager.redis.format.event.FormatResponseEvent
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetFormatStatus
import org.prinstcript10.snippetmanager.snippet.service.SnippetService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component

@Component
class FormatResponseConsumer
    @Autowired
    constructor(
        @Value("\${format_response}")
        private val streamName: String,
        @Value("\${formateando_ando}")
        private val groupName: String,
        private val objectMapper: ObjectMapper,
        private val snippetService: SnippetService,
        private val assetService: AssetService,
        redis: RedisTemplate<String, String>,
    ) : RedisStreamConsumer<String>(streamName, groupName, redis) {

        private val logger = LoggerFactory.getLogger(FormatResponseConsumer::class.java)

        override fun onMessage(record: ObjectRecord<String, String>) {
            val formatResponse: FormatResponseEvent = objectMapper.readValue(record.value)

            logger.info("Received format response: $formatResponse")
            snippetService.updateUserSnippetFormatStatus(
                formatResponse.snippetId,
                formatResponse.userId,
                formatResponse.status,
            )
            if (formatResponse.status == SnippetFormatStatus.SUCCESS && formatResponse.formattedSnippet != null) {
                assetService.saveSnippet(formatResponse.snippetId, formatResponse.formattedSnippet)
            }
        }

        override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
            return StreamReceiver.StreamReceiverOptions.builder()
                .pollTimeout(java.time.Duration.ofMillis(50000))
                .targetType(String::class.java)
                .build()
        }
    }
