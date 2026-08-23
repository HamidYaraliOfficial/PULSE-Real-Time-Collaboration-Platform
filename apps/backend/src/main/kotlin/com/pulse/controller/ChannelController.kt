package com.pulse.controller

import com.pulse.dto.ChannelResponse
import com.pulse.dto.CreateChannelRequest
import com.pulse.service.ChannelService
import com.pulse.util.currentUserId
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/channels")
class ChannelController(private val channelService: ChannelService) {

    @GetMapping
    fun list(@PathVariable workspaceId: UUID): List<ChannelResponse> = channelService.listForUser(workspaceId, currentUserId())

    @PostMapping
    fun create(@PathVariable workspaceId: UUID, @Valid @RequestBody request: CreateChannelRequest): ChannelResponse =
        channelService.create(workspaceId, currentUserId(), request)

    @PostMapping("/{channelId}/read")
    fun markRead(@PathVariable workspaceId: UUID, @PathVariable channelId: UUID) {
        channelService.markRead(channelId, currentUserId())
    }
}
