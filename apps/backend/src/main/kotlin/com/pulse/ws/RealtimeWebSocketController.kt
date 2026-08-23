package com.pulse.ws

import com.pulse.dto.DocumentCollabEvent
import com.pulse.dto.TypingEvent
import com.pulse.service.DocumentService
import com.pulse.service.PresenceService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import java.security.Principal
import java.util.UUID

/**
 * Client -> server STOMP endpoints (prefixed with /app, see WebSocketConfig).
 * These handle the events that don't map naturally to a REST verb:
 * typing indicators, presence heartbeats, and live document cursor/selection
 * broadcast for the collaborative editor.
 */
@Controller
class RealtimeWebSocketController(
    private val messagingTemplate: SimpMessagingTemplate,
    private val presenceService: PresenceService,
    private val documentService: DocumentService
) {

    @MessageMapping("/typing")
    fun typing(event: TypingEvent) {
        messagingTemplate.convertAndSend("/topic/channel.${event.channelId}.typing", event)
    }

    @MessageMapping("/presence/heartbeat")
    fun heartbeat(principal: Principal) {
        presenceService.heartbeat(UUID.fromString(principal.name))
    }

    @MessageMapping("/document/{documentId}/cursor")
    fun cursor(@DestinationVariable documentId: UUID, event: DocumentCollabEvent) {
        documentService.broadcastCursor(event.copy(documentId = documentId))
    }
}
