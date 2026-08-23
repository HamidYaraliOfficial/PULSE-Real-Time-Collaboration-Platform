package com.pulse.ws

import com.pulse.service.PresenceService
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionDisconnectEvent
import java.util.UUID

/** Marks a user offline (in Redis + broadcast) as soon as their socket drops. */
@Component
class WebSocketEventListener(private val presenceService: PresenceService) {

    @EventListener
    fun handleDisconnect(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.wrap(event.message)
        val userId = accessor.user?.name?.let { runCatching { UUID.fromString(it) }.getOrNull() }
        userId?.let { presenceService.markOffline(it) }
    }
}
