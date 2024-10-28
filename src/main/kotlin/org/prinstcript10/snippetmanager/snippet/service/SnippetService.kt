package org.prinstcript10.snippetmanager.snippet.service

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.prinstcript10.snippetmanager.integration.asset.AssetService
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.shared.exception.BadRequestException
import org.prinstcript10.snippetmanager.shared.exception.ConflictException
import org.prinstcript10.snippetmanager.snippet.model.dto.CreateSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.entity.Snippet
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage
import org.prinstcript10.snippetmanager.snippet.repository.SnippetRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SnippetService
    @Autowired
    constructor(
        private val assetService: AssetService,
        private val snippetRepository: SnippetRepository,
        private val runnerServices: Map<SnippetLanguage, RunnerService>,
    ) {

        fun createSnippet(
            createSnippetDTO: CreateSnippetDTO,
            userId: String,
            token: String,
        ) {
            // VALIDATE SNIPPET
            val runnerResponse = runnerServices[createSnippetDTO.language]!!.validateSnippet(
                createSnippetDTO.snippet,
                token,
            )

            if (runnerResponse.statusCode.isError) {
                println(extractMessage(runnerResponse.body!!.toString(), "message"))
                throw BadRequestException(extractMessage(runnerResponse.body!!.toString(), "message"))
            }

            // CREATE SNIPPET
            val snippet = snippetRepository.save(
                Snippet(
                    name = createSnippetDTO.name,
                    language = createSnippetDTO.language,
                ),
            )

            // SAVE SNIPPET TO ASSET SERVICE
            val assetResponse = assetService.saveSnippet(snippet.id!!, createSnippetDTO.snippet)

            if (assetResponse.statusCode.isError) {
                snippetRepository.delete(snippet)
                throw ConflictException("Error saving snippet to asset service")
            }

            // CREATE PERMISSION

            // SHIIIIIIIIII
        }

        fun getSnippet(snippetId: String): String {
            return assetService.getSnippet(snippetId)
        }

        private fun extractMessage(jsonString: String, field: String): String {
            val parsedJsonString = jsonString.substring(jsonString.indexOf("{"), jsonString.length - 1)
            val jsonObject: JsonObject = JsonParser.parseString(parsedJsonString).asJsonObject
            return jsonObject.get(field).asString
        }
    }
