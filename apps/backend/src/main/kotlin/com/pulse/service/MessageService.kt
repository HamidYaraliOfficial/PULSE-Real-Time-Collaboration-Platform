package com.pulse.service

import com.pulse.domain.Message
import com.pulse.domain.MessageContentType
import com.pulse.domain.NotificationType
import com.pulse.dto.MessageResponse
import com.pulse.dto.SendMessageRequest
import com.pulse.exception.ApiException
import com.pulse.repository.*
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID
import java.util.regex.Pattern

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val reactionRepository: ReactionRepository,
    private val userRepository: UserRepository,
    private val channelMemberRepository: ChannelMemberRepository,
    private val channelService: ChannelService,
    private val messagingTemplate: SimpMessagingTemplate,
    private val notificationService: NotificationService
) {
    private val mentionPattern = Pattern.compile("@([a-zA-Z0-9._-]+)")

    @Transactional
    fun send(channelId: UUID, authorId: UUID, request: SendMessageRequest): MessageResponse {
        channelService.requireAccess(channelId, authorId)
        val message = Message(
            channelId = channelId,
            authorId = authorId,
            parentMessageId = request.parentMessageId,
            body = request.body,
            contentType = MessageContentType.valueOf(request.contentType)
        )
        messageRepository.save(message)

        val response = toResponse(message)
        // Broadcast to everyone subscribed to this channel's topic in real time.
        messagingTemplate.convertAndSend("/topic/channel.$channelId", response)

        notifyMentions(channelId, authorId, request.body)
        return response
    }

    fun history(channelId: UUID, userId: UUID): List<MessageResponse> {
        channelService.requireAccess(channelId, userId)
        return messageRepository.findAllByChannelIdAndIsDeletedFalseOrderByCreatedAtDesc(channelId)
            .filter { it.parentMessageId == null }
            .map { toResponse(it) }
    }

    fun thread(parentMessageId: UUID, userId: UUID): List<MessageResponse> =
        messageRepository.findAllByParentMessageIdOrderByCreatedAtAsc(parentMessageId).map { toResponse(it) }

    fun search(channelId: UUID, userId: UUID, query: String): List<MessageResponse> {
        channelService.requireAccess(channelId, userId)
        return messageRepository.searchInChannel(channelId, query).map { toResponse(it) }
    }

    @Transactional
    fun edit(messageId: UUID, userId: UUID, body: String): MessageResponse {
        val message = messageRepository.findById(messageId).orElseThrow { ApiException(404, "Message not found") }
        if (message.authorId != userId) throw ApiException(403, "You can only edit your own messages")
        message.body = body
        message.isEdited = true
        message.updatedAt = Instant.now()
        messageRepository.save(message)
        val response = toResponse(message)
        messagingTemplate.convertAndSend("/topic/channel.${message.channelId}", response)
        return response
    }

    @Transactional
    fun delete(messageId: UUID, userId: UUID) {
        val message = messageRepository.findById(messageId).orElseThrow { ApiException(404, "Message not found") }
        if (message.authorId != userId) throw ApiException(403, "You can only delete your own messages")
        message.isDeleted = true
        messageRepository.save(message)
        messagingTemplate.convertAndSend("/topic/channel.${message.channelId}", mapOf("deletedMessageId" to messageId))
    }

    @Transactional
    fun togglePin(messageId: UUID, userId: UUID): MessageResponse {
        val message = messageRepository.findById(messageId).orElseThrow { ApiException(404, "Message not found") }
        message.isPinned = !message.isPinned
        messageRepository.save(message)
        return toResponse(message)
    }

    @Transactional
    fun react(messageId: UUID, userId: UUID, emoji: String) {
        val message = messageRepository.findById(messageId).orElseThrow { ApiException(404, "Message not found") }
        reactionRepository.save(com.pulse.domain.Reaction(messageId = messageId, userId = userId, emoji = emoji))
        messagingTemplate.convertAndSend("/topic/channel.${message.channelId}", toResponse(message))
    }

    private fun notifyMentions(channelId: UUID, authorId: UUID, body: String) {
        val matcher = mentionPattern.matcher(body)
        val mentioned = mutableSetOf<String>()
        while (matcher.find()) mentioned.add(matcher.group(1))
        if (mentioned.isEmpty()) return

        val everyone = mentioned.contains("everyone") || mentioned.contains("here")
        val recipients = if (everyone) {
            channelMemberRepository.findAllByChannelId(channelId).map { it.userId }.filter { it != authorId }
        } else {
            userRepository.findAll()
                .filter { it.displayName.replace(" ", "").lowercase() in mentioned.map { m -> m.lowercase() } }
                .map { it.id!! }
        }
        recipients.forEach { uid ->
            notificationService.create(uid, NotificationType.MENTION, "You were mentioned", body.take(140), "/chat/$channelId")
        }
    }

    private fun toResponse(message: Message): MessageResponse {
        val author = userRepository.findById(message.authorId).orElse(null)
        val reactions = reactionRepository.findAllByMessageId(message.id!!).groupingBy { it.emoji }.eachCount()
        val replyCount = messageRepository.findAllByParentMessageIdOrderByCreatedAtAsc(message.id!!).size
        return MessageResponse(
            id = message.id!!, channelId = message.channelId, authorId = message.authorId,
            authorName = author?.displayName ?: "Unknown", authorAvatarUrl = author?.avatarUrl,
            body = message.body, parentMessageId = message.parentMessageId, replyCount = replyCount,
            isPinned = message.isPinned, isEdited = message.isEdited, reactions = reactions,
            createdAt = message.createdAt
        )
    }
}
