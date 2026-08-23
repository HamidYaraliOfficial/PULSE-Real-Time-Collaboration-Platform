package com.pulse.service

import com.pulse.domain.Channel
import com.pulse.domain.ChannelMember
import com.pulse.domain.ChannelType
import com.pulse.dto.ChannelResponse
import com.pulse.dto.CreateChannelRequest
import com.pulse.exception.ApiException
import com.pulse.repository.ChannelMemberRepository
import com.pulse.repository.ChannelRepository
import com.pulse.repository.MessageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ChannelService(
    private val channelRepository: ChannelRepository,
    private val channelMemberRepository: ChannelMemberRepository,
    private val messageRepository: MessageRepository,
    private val workspaceService: WorkspaceService,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun create(workspaceId: UUID, actorId: UUID, request: CreateChannelRequest): ChannelResponse {
        workspaceService.requireMembership(workspaceId, actorId)
        val channel = Channel(
            workspaceId = workspaceId,
            teamId = request.teamId,
            name = request.name,
            topic = request.topic,
            type = ChannelType.valueOf(request.type),
            createdBy = actorId
        )
        channelRepository.save(channel)

        val memberIds = (request.memberIds + actorId).distinct()
        memberIds.forEach { uid ->
            channelMemberRepository.save(ChannelMember(channelId = channel.id!!, userId = uid))
        }
        auditLogService.log(workspaceId, actorId, "CHANNEL_CREATED", "CHANNEL", channel.id)
        return toResponse(channel, actorId)
    }

    fun listForUser(workspaceId: UUID, userId: UUID): List<ChannelResponse> {
        val memberChannelIds = channelMemberRepository.findAllByUserId(userId).map { it.channelId }.toSet()
        return channelRepository.findAllByWorkspaceIdAndIsArchivedFalse(workspaceId)
            .filter { it.type == ChannelType.PUBLIC || it.id in memberChannelIds }
            .map { toResponse(it, userId) }
    }

    fun get(channelId: UUID, userId: UUID): Channel {
        requireAccess(channelId, userId)
        return channelRepository.findById(channelId).orElseThrow { ApiException(404, "Channel not found") }
    }

    fun requireAccess(channelId: UUID, userId: UUID) {
        val channel = channelRepository.findById(channelId).orElseThrow { ApiException(404, "Channel not found") }
        if (channel.type == ChannelType.PUBLIC) return
        channelMemberRepository.findByChannelIdAndUserId(channelId, userId)
            ?: throw ApiException(403, "You do not have access to this channel")
    }

    @Transactional
    fun markRead(channelId: UUID, userId: UUID) {
        val member = channelMemberRepository.findByChannelIdAndUserId(channelId, userId)
            ?: channelMemberRepository.save(ChannelMember(channelId = channelId, userId = userId))
        member.lastReadAt = java.time.Instant.now()
        channelMemberRepository.save(member)
    }

    private fun toResponse(channel: Channel, userId: UUID): ChannelResponse {
        val member = channelMemberRepository.findByChannelIdAndUserId(channel.id!!, userId)
        val messages = messageRepository.findAllByChannelIdAndIsDeletedFalseOrderByCreatedAtDesc(channel.id!!)
        val unread = if (member?.lastReadAt != null) {
            messages.count { it.createdAt.isAfter(member.lastReadAt) }
        } else messages.size

        return ChannelResponse(
            id = channel.id!!, name = channel.name, topic = channel.topic, type = channel.type.name,
            unreadCount = unread, lastMessageAt = messages.firstOrNull()?.createdAt
        )
    }
}
