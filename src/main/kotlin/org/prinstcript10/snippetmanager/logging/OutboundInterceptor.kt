package org.prinstcript10.snippetmanager.logging

import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class OutboundInterceptor : ClientHttpRequestInterceptor {
    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val correlationId = MDC.get(InboundFilter.TRACE_ID_KEY) ?: UUID.randomUUID().toString()
        request.headers.add(InboundFilter.TRACE_ID_HEADER, correlationId)
        return execution.execute(request, body)
    }
}
