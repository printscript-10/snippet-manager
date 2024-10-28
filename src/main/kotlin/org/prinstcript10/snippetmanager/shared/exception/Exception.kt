package org.prinstcript10.snippetmanager.shared.exception

import org.springframework.http.HttpStatus

open class HttpException(val status: HttpStatus, message: String) : RuntimeException(message)

class BadRequestException(message: String) : HttpException(HttpStatus.BAD_REQUEST, message)

class NotFoundException(message: String) : HttpException(HttpStatus.NOT_FOUND, message)

class ConflictException(message: String) : HttpException(HttpStatus.CONFLICT, message)
