package org.prinstcript10.snippetmanager.snippet.service

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import jakarta.transaction.Transactional
import org.prinstcript10.snippetmanager.integration.asset.AssetService
import org.prinstcript10.snippetmanager.integration.auth0.Auth0Service
import org.prinstcript10.snippetmanager.integration.auth0.dto.PaginatedUsersDTO
import org.prinstcript10.snippetmanager.integration.permission.PermissionService
import org.prinstcript10.snippetmanager.integration.permission.SnippetOwnership
import org.prinstcript10.snippetmanager.integration.runner.RunnerService
import org.prinstcript10.snippetmanager.shared.exception.BadRequestException
import org.prinstcript10.snippetmanager.shared.exception.ConflictException
import org.prinstcript10.snippetmanager.shared.exception.NotFoundException
import org.prinstcript10.snippetmanager.snippet.model.dto.CreateSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.EditSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.GetSnippetLanguageDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.PaginatedSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.PaginatedSnippetsDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.ShareSnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.dto.SnippetDTO
import org.prinstcript10.snippetmanager.snippet.model.entity.Snippet
import org.prinstcript10.snippetmanager.snippet.model.entity.UserSnippetFormatting
import org.prinstcript10.snippetmanager.snippet.model.entity.UserSnippetLinting
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetFormatStatus
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLanguage
import org.prinstcript10.snippetmanager.snippet.model.enum.SnippetLintingStatus
import org.prinstcript10.snippetmanager.snippet.repository.SnippetRepository
import org.prinstcript10.snippetmanager.snippet.repository.UserSnippetFormattingRepository
import org.prinstcript10.snippetmanager.snippet.repository.UserSnippetLintingRepository
import org.prinstcript10.snippetmanager.testCase.service.TestCaseService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SnippetService
    @Autowired
    constructor(
        private val assetService: AssetService,
        private val snippetRepository: SnippetRepository,
        private val userSnippetLintingRepository: UserSnippetLintingRepository,
        private val userSnippetFormattingRepository: UserSnippetFormattingRepository,
        private val runnerServices: Map<SnippetLanguage, RunnerService>,
        private val permissionService: PermissionService,
        private val auth0Service: Auth0Service,
        private val testCaseService: TestCaseService,

    ) {
        private val logger = LoggerFactory.getLogger(SnippetService::class.java)

        suspend fun createSnippet(
            createSnippetDTO: CreateSnippetDTO,
            userId: String,
            token: String,
        ): SnippetDTO {
            // VALIDATE SNIPPET
            logger.info(
                "Creating snippet with name: ${createSnippetDTO.name}, language: ${createSnippetDTO.language}",
            )
            logger.info("Validating snippet ${createSnippetDTO.name}")
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
            logger.info("Saving snippet: ${createSnippetDTO.name} to asset service")
            val assetResponse = assetService.saveSnippet(snippet.id!!, createSnippetDTO.snippet)

            if (assetResponse.statusCode.isError) {
                logger.error("Error saving snippet to assetService:" + assetResponse.body)
                snippetRepository.delete(snippet)
                throw ConflictException("Error saving snippet to asset service")
            }

            // CREATE PERMISSION
            logger.info("Creating snippet permission for snippet: ${createSnippetDTO.name}")
            val permission = permissionService.createPermission(snippet.id, token)

            if (permission.statusCode.isError) {
                logger.error("Error creating snippet permission:" + permission.body)
                snippetRepository.delete(snippet)
                throw ConflictException(extractMessage(permission.body!!.toString(), "message"))
            }

            createUserPendingSnippetLint(userId, snippet)
            createUserPendingSnippetFormat(userId, snippet)

            return SnippetDTO(
                id = snippet.id,
                name = snippet.name,
                language = createSnippetDTO.language,
                extension = snippet.language.getExtension(),
                content = createSnippetDTO.snippet,
            )
        }

        fun getSnippet(snippetId: String, token: String): SnippetDTO {
            logger.info("Getting snippet: $snippetId")
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            permissionService.getPermission(snippetId, token)

            val snippet = assetService.getSnippet(snippetId)

            return SnippetDTO(
                id = snippetId,
                name = existingSnippet.name,
                language = existingSnippet.language,
                extension = existingSnippet.language.getExtension(),
                content = snippet,
            )
        }

        fun getSnippetLanguages(): List<GetSnippetLanguageDTO> {
            logger.info("Getting languages")
            return SnippetLanguage.entries.map {
                GetSnippetLanguageDTO(
                    language = it.name,
                    extension = it.getExtension(),
                )
            }
        }

        fun getAllSnippets(
            token: String,
            page: Int,
            pageSize: Int,
            param: String,
            userId: String,
        ): PaginatedSnippetsDTO {
            logger.info("Getting all snippets for user: $userId")
            val offset = page * pageSize
            val response = permissionService.getAllSnippetPermissions(token)

            val snippetPermissions = response.body
                ?: throw BadRequestException("Unexpected response format")

            val snippetIds: List<String> = snippetPermissions.map { it.snippetId }

            val snippets = snippetRepository.findAll(snippetIds, pageSize, offset, param)

            val nickname: String = auth0Service.getUserById(userId).body!!.nickname

            return PaginatedSnippetsDTO(
                snippets = snippets.map {
                    val status: String =
                        userSnippetLintingRepository.findFirstBySnippetIdAndUserId(it.id!!, userId)?.status?.name
                            ?: SnippetLintingStatus.PENDING.name

                    val author: String =
                        if (
                            snippetPermissions.find {
                                    permission ->
                                permission.snippetId == it.id
                            }!!.ownership == SnippetOwnership.OWNER
                        ) {
                            nickname
                        } else {
                            auth0Service.getUserById(
                                permissionService.getSnippetOwner(it.id, token).body!!.userId,
                            ).body?.nickname!!
                        }

                    PaginatedSnippetDTO(
                        id = it.id,
                        name = it.name,
                        language = it.language.name,
                        extension = it.language.getExtension(),
                        compliance = status,
                        author = author,
                    )
                },
            )
        }

        @Transactional
        suspend fun updateSnippet(editSnippetDTO: EditSnippetDTO, snippetId: String, token: String) {
            logger.info("Updating snippet: $snippetId")
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            permissionService.getPermission(snippetId, token)

//            val permission = permissionService.getPermission(snippetId, token)
//
//            val ownership = permission.body!!.ownership
//
//            if (ownership != SnippetOwnership.OWNER) {
//                throw BadRequestException(
//                    "User is not the snippet owner",
//                )
//            }
            logger.info("Validating snippet: $snippetId")
            runnerServices[existingSnippet.language]!!.validateSnippet(
                editSnippetDTO.snippet,
                token,
            )

            val tests: List<String> = testCaseService.resetSnippetTesting(snippetId)
            testCaseService.publishSnippetTestingEvents(tests)
            logger.info("Saving updated snippet: $snippetId")
            assetService.saveSnippet(snippetId, editSnippetDTO.snippet)
        }

        fun deleteSnippet(snippetId: String, token: String) {
            logger.info("Deleting snippet: $snippetId")
            val existingSnippet = snippetRepository.findById(snippetId)
                .orElseThrow { NotFoundException("Snippet with ID $snippetId not found") }

            permissionService.deleteSnippetPermissions(snippetId, token)

            assetService.deleteSnippet(snippetId)

            snippetRepository.delete(existingSnippet)
        }

        suspend fun shareSnippet(shareSnippetDTO: ShareSnippetDTO, token: String) {
            logger.info("Sharing snippet: ${shareSnippetDTO.snippetId} to user: ${shareSnippetDTO.userId}")
            val snippet = snippetRepository.findById(shareSnippetDTO.snippetId)
                .orElseThrow { NotFoundException("Snippet with ID ${shareSnippetDTO.snippetId} not found") }

            permissionService.shareSnippet(shareSnippetDTO, token)
            createUserPendingSnippetLint(shareSnippetDTO.userId, snippet)
            createUserPendingSnippetFormat(shareSnippetDTO.userId, snippet)
        }

        fun getSnippetFriends(page: Int, pageSize: Int, param: String, userId: String): PaginatedUsersDTO {
            logger.info("Getting snippet friends for user: $userId")
            val users = auth0Service.getUsers(page, pageSize, param).body!!.filter { it.user_id != userId }
            return PaginatedUsersDTO(
                users = users,
                total = users.size,
            )
        }

        fun resetUserSnippetLinting(userId: String): List<String> {
            logger.info("Resetting snippet linting for user: $userId")
            val snippets = userSnippetLintingRepository.findAllByUserId(userId)
            snippets.forEach {
                it.status = SnippetLintingStatus.PENDING
            }

            userSnippetLintingRepository.saveAll(snippets)

            return snippets.map { it.snippet!!.id!! }
        }

        fun resetUserSnippetFormatting(userId: String): List<String> {
            logger.info("Resetting snippet formatting for user: $userId")
            val snippets = userSnippetFormattingRepository.findAllByUserId(userId)
            snippets.forEach {
                it.status = SnippetFormatStatus.PENDING
            }
            userSnippetFormattingRepository.saveAll(snippets)

            return snippets.map { it.snippet!!.id!! }
        }

        @Transactional
        fun updateUserSnippetLintingStatus(snippetId: String, userId: String, status: SnippetLintingStatus) {
            logger.info("Updating snippet linting status for user: $userId")
            userSnippetLintingRepository.findFirstBySnippetIdAndUserId(snippetId, userId)?.let {
                it.status = status
                userSnippetLintingRepository.save(it)
            }
        }

        @Transactional
        fun updateUserSnippetFormatStatus(snippetId: String, userId: String, status: SnippetFormatStatus) {
            logger.info("Updating snippet formatting status for user: $userId")
            userSnippetFormattingRepository.findFirstBySnippetIdAndUserId(snippetId, userId)?.let {
                it.status = status
                userSnippetFormattingRepository.save(it)
            }
        }

        private fun createUserPendingSnippetFormat(userId: String, snippet: Snippet) {
            userSnippetFormattingRepository.save(
                UserSnippetFormatting(
                    userId = userId,
                    snippet = snippet,
                    status = SnippetFormatStatus.PENDING,
                ),
            )
        }

        private fun createUserPendingSnippetLint(userId: String, snippet: Snippet) {
            userSnippetLintingRepository.save(
                UserSnippetLinting(
                    userId = userId,
                    snippet = snippet,
                    status = SnippetLintingStatus.PENDING,
                ),
            )
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
