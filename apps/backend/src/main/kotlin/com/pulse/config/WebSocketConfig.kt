package com.pulse.config

import com.pulse.security.StompAuthChannelInterceptor
import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

/**
 * STOMP over WebSocket configuration.
 * Real-time chat, presence, typing indicators, task-board updates and
 * document collaboration events are all published through this broker
 * so that any number of backend instances behind a load balancer can be
 * scaled horizontally once the "relay" broker below is swapped for an
 * external broker (RabbitMQ / ActiveMQ) instead of the built-in simple broker.
 */
@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(private val stompAuthChannelInterceptor: StompAuthChannelInterceptor) : WebSocketMessageBrokerConfigurer {

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        // Topics: /topic/channel.{id}, /topic/presence, /topic/document.{id}, /topic/kanban.{projectId}
        // Per-user queue: /user/queue/notifications
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOriginPatterns("*")
            .withSockJS()
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(stompAuthChannelInterceptor)
    }
}
