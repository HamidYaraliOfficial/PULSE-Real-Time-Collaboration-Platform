package com.pulse.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "documents")
class DocumentEntity(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "parent_id")
    var parentId: UUID? = null,

    var title: String,

    @Column(columnDefinition = "jsonb")
    var content: String = """{"blocks": []}""",

    var icon: String? = null,

    @Column(name = "is_favorite")
    var isFavorite: Boolean = false,

    @Column(name = "created_by")
    var createdBy: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "document_versions")
class DocumentVersion(
    @Column(name = "document_id")
    var documentId: UUID,

    @Column(columnDefinition = "jsonb")
    var content: String,

    @Column(name = "author_id")
    var authorId: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "document_comments")
class DocumentComment(
    @Column(name = "document_id")
    var documentId: UUID,

    @Column(name = "author_id")
    var authorId: UUID,

    @Column(name = "block_id")
    var blockId: String? = null,

    @Column(columnDefinition = "text")
    var body: String,

    var resolved: Boolean = false,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "collaboration_sessions")
class CollaborationSession(
    @Column(name = "document_id")
    var documentId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    @Column(name = "cursor_position", columnDefinition = "jsonb")
    var cursorPosition: String? = null,

    @Column(name = "started_at")
    var startedAt: Instant = Instant.now(),

    @Column(name = "last_seen_at")
    var lastSeenAt: Instant = Instant.now()
) : BaseEntity()
