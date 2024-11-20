package org.prinstcript10.snippetmanager.logging

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.UUID

@Component
@Order(1)
class InboundFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            val traceId = request.getHeader(TRACE_ID_HEADER) ?: UUID.randomUUID().toString()

            MDC.put(TRACE_ID_KEY, traceId)

            response.setHeader(TRACE_ID_HEADER, traceId)
            try {
                chain.doFilter(request, response)
            } finally {
                MDC.remove(TRACE_ID_KEY)
            }
        } else {
            chain.doFilter(request, response)
        }
    }
    companion object {
        const val TRACE_ID_KEY: String = "correlation-id"
        const val TRACE_ID_HEADER: String = "X-Correlation-Id"
    }
}
