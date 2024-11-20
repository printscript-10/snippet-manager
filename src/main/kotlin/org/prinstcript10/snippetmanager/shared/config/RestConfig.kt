package org.prinstcript10.snippetmanager.shared.config

import org.prinstcript10.snippetmanager.logging.OutboundInterceptor
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestConfig {
    @Bean
    fun restTemplate(outboundInterceptor: OutboundInterceptor): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(outboundInterceptor)
        return restTemplate
    }
}
