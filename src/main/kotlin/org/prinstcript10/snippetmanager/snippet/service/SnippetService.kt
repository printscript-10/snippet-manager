package org.prinstcript10.snippetmanager.snippet.service

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.prinstcript10.snippetmanager.integration.asset.AssetService
import org.prinstcript10.snippetmanager.integration.permission.PermissionService
import org.prinstcript10.snippetmanager.integration.permission.SnippetOwnership
import org.prinstcript10.snippetmanager.integration.permission.SnippetPermissionDTO
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.shared.exception.BadRequestException
import org.prinstcript10.snippetmanager.shared.exception.ConflictException
import org.prinstcript10.snippetmanager.shared.exception.NotFoundException
import org.prinstcript10.snippetmanager.snippet.model.dto.CreateSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.EditSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.ShareSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.SnippetDTO
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
            val permission = permissionService.createPermission(snippet.id, token)

            if (permission.statusCode.isError) {
                snippetRepository.delete(snippet)
                throw ConflictException(extractMessage(permission.body!!.toString(), "message"))
            }
        }

        fun getSnippet(snippetId: String, token: String): SnippetDTO {
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            val permission = permissionService.getPermission(snippetId, token)
            if (permission.statusCode.isError) {
                throw BadRequestException(extractMessage(permission.body!!.toString(), "message"))
            }

            val snippet = assetService.getSnippet(snippetId)

            return SnippetDTO(existingSnippet.name, existingSnippet.language, snippet)
        }

        fun getAllSnippets(token: String): List<SnippetDTO> {
            val response = permissionService.getAllSnippetPermissions(token)
            if (response.statusCode.isError) {
                throw BadRequestException("Failed to retrieve permissions: ${response.body}")
            }

            val snippetPermissions = response.body as? List<SnippetPermissionDTO>
                ?: throw BadRequestException("Unexpected response format")

            val snippets = mutableListOf<SnippetDTO>()

            for (permission in snippetPermissions) {
                val existingSnippet = snippetRepository.findById(permission.snippetId)
                    .orElseThrow { NotFoundException("Snippet with ID ${permission.snippetId} not found") }

                val snippetContent = assetService.getSnippet(permission.snippetId)

                val snippetDTO = SnippetDTO(
                    name = existingSnippet.name,
                    language = existingSnippet.language,
                    snippet = snippetContent,
                )
                snippets.add(snippetDTO)
            }

            return snippets
        }

        fun updateSnippet(editSnippetDTO: EditSnippetDTO, snippetId: String, token: String) {
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            val permission = permissionService.getPermission(snippetId, token)

            if (permission.statusCode.isError) {
                throw BadRequestException(extractMessage(permission.body!!.toString(), "message"))
            }

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

        fun deleteSnippet(snippetId: String, token: String) {
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            val permission = permissionService.deleteSnippetPermissions(snippetId, token)

            if (permission.statusCode.isError) {
                throw ConflictException(extractMessage(permission.body!!.toString(), "message"))
            }

            val assetResponse = assetService.deleteSnippet(snippetId)

            if (assetResponse.statusCode.isError) {
                throw ConflictException("Error deleting snippet from asset service")
            }

            snippetRepository.delete(existingSnippet)
        }

        fun shareSnippet(shareSnippetDTO: ShareSnippetDTO, token: String) {
            val existingSnippet = snippetRepository.findById(shareSnippetDTO.snippetId)
                .orElseThrow { NotFoundException("Snippet with ID ${shareSnippetDTO.snippetId} not found") }

            val permission = permissionService.shareSnippet(shareSnippetDTO, token)

            if (permission.statusCode.isError) {
                throw ConflictException(extractMessage(permission.body!!.toString(), "message"))
            }
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
