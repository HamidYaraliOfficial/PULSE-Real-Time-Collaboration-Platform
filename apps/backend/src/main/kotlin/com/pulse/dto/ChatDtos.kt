package com.pulse.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateChannelRequest(
    @field:NotBlank val name: String,
    val topic: String? = null,
    val type: String = "PUBLIC",
    val teamId: UUID? = null,
    val memberIds: List<UUID> = emptyList()
)

data class ChannelResponse(
    val id: UUID,
    val name: String,
    val topic: String?,
    val type: String,
    val unreadCount: Int,
    val lastMessageAt: Instant?
)

data class SendMessageRequest(
    @field:NotBlank val body: String,
    val parentMessageId: UUID? = null,
    val contentType: String = "MARKDOWN",
    val attachmentFileIds: List<UUID> = emptyList()
)

data class MessageResponse(
    val id: UUID,
    val channelId: UUID,
    val authorId: UUID,
    val authorName: String,
    val authorAvatarUrl: String?,
    val body: String,
    val parentMessageId: UUID?,
    val replyCount: Int,
    val isPinned: Boolean,
    val isEdited: Boolean,
    val reactions: Map<String, Int>,
    val createdAt: Instant
)

data class TypingEvent(val channelId: UUID, val userId: UUID, val displayName: String, val isTyping: Boolean)

data class PresenceEvent(val userId: UUID, val status: String, val lastActiveAt: Instant?)

data class ReactionRequest(val emoji: String)
