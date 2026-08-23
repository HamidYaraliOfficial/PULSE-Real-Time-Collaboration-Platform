package com.pulse.controller

import com.pulse.dto.MessageResponse
import com.pulse.dto.ReactionRequest
import com.pulse.dto.SendMessageRequest
import com.pulse.service.MessageService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/channels/{channelId}/messages")
class MessageController(private val messageService: MessageService) {

    @GetMapping
    fun history(@PathVariable channelId: UUID): List<MessageResponse> = messageService.history(channelId, currentUserId())

    @PostMapping
    fun send(@PathVariable channelId: UUID, @Valid @RequestBody request: SendMessageRequest): MessageResponse =
        messageService.send(channelId, currentUserId(), request)

    @GetMapping("/search")
    fun search(@PathVariable channelId: UUID, @RequestParam q: String): List<MessageResponse> =
        messageService.search(channelId, currentUserId(), q)

    @GetMapping("/{messageId}/thread")
    fun thread(@PathVariable channelId: UUID, @PathVariable messageId: UUID): List<MessageResponse> =
        messageService.thread(messageId, currentUserId())

    @PatchMapping("/{messageId}")
    fun edit(@PathVariable channelId: UUID, @PathVariable messageId: UUID, @RequestBody body: Map<String, String>): MessageResponse =
        messageService.edit(messageId, currentUserId(), body["body"] ?: "")

    @DeleteMapping("/{messageId}")
    fun delete(@PathVariable channelId: UUID, @PathVariable messageId: UUID) {
        messageService.delete(messageId, currentUserId())
    }

    @PostMapping("/{messageId}/pin")
    fun pin(@PathVariable channelId: UUID, @PathVariable messageId: UUID): MessageResponse =
        messageService.togglePin(messageId, currentUserId())

    @PostMapping("/{messageId}/reactions")
    fun react(@PathVariable channelId: UUID, @PathVariable messageId: UUID, @RequestBody request: ReactionRequest) {
        messageService.react(messageId, currentUserId(), request.emoji)
    }
}
