package com.pulse.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "pulse.cors")
class CorsProperties {
    var allowedOrigins: String = "http://localhost:3000"
}
