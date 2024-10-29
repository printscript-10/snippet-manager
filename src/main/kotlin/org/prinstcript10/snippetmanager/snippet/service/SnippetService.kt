package org.prinstcript10.snippetmanager.snippet.service

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.prinstcript10.snippetmanager.integration.asset.AssetService
import org.prinstcript10.snippetmanager.integration.permission.PermissionService
import org.prinstcript10.snippetmanager.integration.permission.SnippetOwnership
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.shared.exception.BadRequestException
import org.prinstcript10.snippetmanager.shared.exception.ConflictException
import org.prinstcript10.snippetmanager.shared.exception.NotFoundException
import org.prinstcript10.snippetmanager.snippet.model.dto.CreateSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.EditSnippetDTO
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
        private val permissionService: PermissionService,
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
            permissionService.createPermission(snippet.id, SnippetOwnership.OWNER, token)
        }

        fun getSnippet(snippetId: String, token: String): String {
            val permission = permissionService.getPermission(snippetId, token)
            if (permission.statusCode.isError) {
                throw BadRequestException("User does not have permission to access this resource")
            }
            return assetService.getSnippet(snippetId)
        }

        fun updateSnippet(editSnippetDTO: EditSnippetDTO, snippetId: String, userId: String, token: String) {
            var existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            val permission = permissionService.getPermission(snippetId, token)

            val ownership = extractMessage(permission.body!!.toString(), "ownership")

            if (ownership != SnippetOwnership.OWNER.toString()) {
                throw BadRequestException(
                    "User is not the snippet owner",
                )
            }

            val runnerResponse = runnerServices[existingSnippet.language]!!.validateSnippet(
                editSnippetDTO.snippet,
                token,
            )

            if (runnerResponse.statusCode.isError) {
                throw BadRequestException(extractMessage(runnerResponse.body!!.toString(), "message"))
            }

            existingSnippet.name = editSnippetDTO.name

            assetService.saveSnippet(snippetId, editSnippetDTO.snippet)

            snippetRepository.save(existingSnippet)
        }

        private fun extractMessage(jsonString: String, field: String): String {
            val startIndex = jsonString.indexOf("{")
            val endIndex = jsonString.indexOf("}", startIndex)

            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                throw ConflictException("Invalid JSON string")
            }

            val parsedJsonString = jsonString.substring(startIndex, endIndex + 1)

            val jsonObject: JsonObject = JsonParser.parseString(parsedJsonString).asJsonObject

            val field = jsonObject.get(field)?.asString

            if (field == null) throw ConflictException("Invalid field")

            return field
        }
    }
