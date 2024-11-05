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
            runnerServices[createSnippetDTO.language]!!.validateSnippet(
                createSnippetDTO.snippet,
                token,
            )

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

            permissionService.getPermission(snippetId, token)

            val snippet = assetService.getSnippet(snippetId)

            return SnippetDTO(existingSnippet.name, existingSnippet.language, snippet)
        }

        fun getAllSnippets(token: String, page: Int, pageSize: Int, param: String): List<Snippet> {
            val offset = page * pageSize
            val response = permissionService.getAllSnippetPermissions(token)

            val snippetPermissions = response.body
                ?: throw BadRequestException("Unexpected response format")

            val snippetIds: List<String> = snippetPermissions.map { it.snippetId }

            val snippets = snippetRepository.findAll(snippetIds, pageSize, offset, param)

            return snippets
        }

        fun updateSnippet(editSnippetDTO: EditSnippetDTO, snippetId: String, token: String) {
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            val permission = permissionService.getPermission(snippetId, token)

            val ownership = permission.body!!.ownership

            if (ownership != SnippetOwnership.OWNER) {
                throw BadRequestException(
                    "User is not the snippet owner",
                )
            }

            runnerServices[existingSnippet.language]!!.validateSnippet(
                editSnippetDTO.snippet,
                token,
            )

            existingSnippet.name = editSnippetDTO.name

            assetService.saveSnippet(snippetId, editSnippetDTO.snippet)

            snippetRepository.save(existingSnippet)
        }

        fun deleteSnippet(snippetId: String, token: String) {
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            permissionService.deleteSnippetPermissions(snippetId, token)

            assetService.deleteSnippet(snippetId)

            snippetRepository.delete(existingSnippet)
        }

        fun shareSnippet(shareSnippetDTO: ShareSnippetDTO, token: String) {
            snippetRepository.findById(shareSnippetDTO.snippetId)
                .orElseThrow { NotFoundException("Snippet with ID ${shareSnippetDTO.snippetId} not found") }

            permissionService.shareSnippet(shareSnippetDTO, token)
        }

        private fun extractMessage(jsonString: String, field: String): String {
            val startIndex = jsonString.indexOf("{")
            val endIndex = jsonString.indexOf("}", startIndex)

            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) {
                throw ConflictException("Invalid JSON string")
            }

            val parsedJsonString = jsonString.substring(startIndex, endIndex + 1)

            val jsonObject: JsonObject = JsonParser.parseString(parsedJsonString).asJsonObject

            val res = jsonObject.get(field)?.asString

            if (res == null) throw ConflictException("Invalid field")

            return res
        }
    }
