package org.prinstcript10.snippetmanager.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException

@Component
@Order(2)
class RequestTraceFilter : Filter {

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            val uri = request.requestURI
            val method = request.method
            val logPrefix = "$method $uri"

            try {
                chain.doFilter(request, response)
            } finally {
                val statusCode = response.status

                val traceId = MDC.get(InboundFilter.TRACE_ID_KEY) ?: "none"

                logger.info("[{}] {} - {}", traceId, logPrefix, statusCode)
            }
        } else {
            chain.doFilter(request, response)
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(RequestTraceFilter::class.java)
    }
}
