package com.pulse.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "channels")
class Channel(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "team_id")
    var teamId: UUID? = null,

    var name: String,
    var topic: String? = null,

    @Enumerated(EnumType.STRING)
    var type: ChannelType = ChannelType.PUBLIC,

    @Column(name = "is_archived")
    var isArchived: Boolean = false,

    @Column(name = "created_by")
    var createdBy: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "channel_members")
class ChannelMember(
    @Column(name = "channel_id")
    var channelId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    @Column(name = "last_read_at")
    var lastReadAt: Instant? = null,

    @Column(name = "is_muted")
    var isMuted: Boolean = false,

    @Column(name = "joined_at")
    var joinedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "messages")
class Message(
    @Column(name = "channel_id")
    var channelId: UUID,

    @Column(name = "author_id")
    var authorId: UUID,

    @Column(name = "parent_message_id")
    var parentMessageId: UUID? = null,

    @Column(columnDefinition = "text")
    var body: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "content_type")
    var contentType: MessageContentType = MessageContentType.MARKDOWN,

    @Column(name = "is_pinned")
    var isPinned: Boolean = false,

    @Column(name = "is_edited")
    var isEdited: Boolean = false,

    @Column(name = "is_deleted")
    var isDeleted: Boolean = false,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "reactions")
class Reaction(
    @Column(name = "message_id")
    var messageId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    var emoji: String,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "message_attachments")
class MessageAttachment(
    @Column(name = "message_id")
    var messageId: UUID,

    @Column(name = "file_id")
    var fileId: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()
