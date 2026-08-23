package com.pulse.domain

import jakarta.persistence.*
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "files")
class FileAsset(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "uploader_id")
    var uploaderId: UUID,

    @Column(name = "file_name")
    var fileName: String,

    @Column(name = "mime_type")
    var mimeType: String,

    @Column(name = "size_bytes")
    var sizeBytes: Long,

    @Column(name = "storage_key")
    var storageKey: String,

    @Column(name = "checksum_sha256")
    var checksumSha256: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "context_type")
    var contextType: FileContextType? = null,

    @Column(name = "context_id")
    var contextId: UUID? = null,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "notifications")
class NotificationEntity(
    @Column(name = "user_id")
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    var type: NotificationType,

    var title: String,

    @Column(columnDefinition = "text")
    var body: String? = null,

    var link: String? = null,

    @Column(name = "is_read")
    var isRead: Boolean = false,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "integrations")
class Integration(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Enumerated(EnumType.STRING)
    var provider: IntegrationProvider,

    @Column(columnDefinition = "jsonb")
    var config: String = "{}",

    @Column(name = "webhook_secret")
    var webhookSecret: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "connected_by")
    var connectedBy: UUID,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "ai_sessions")
class AiSession(
    @Column(name = "user_id")
    var userId: UUID,

    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "context_type")
    var contextType: String? = null,

    @Column(name = "context_id")
    var contextId: UUID? = null,

    @Column(columnDefinition = "text")
    var prompt: String,

    @Column(columnDefinition = "text")
    var response: String? = null,

    @Column(name = "action_taken")
    var actionTaken: String? = null,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "audit_logs")
class AuditLog(
    @Column(name = "workspace_id")
    var workspaceId: UUID? = null,

    @Column(name = "actor_id")
    var actorId: UUID? = null,

    var action: String,

    @Column(name = "entity_type")
    var entityType: String,

    @Column(name = "entity_id")
    var entityId: UUID? = null,

    @Column(columnDefinition = "jsonb")
    var metadata: String = "{}",

    @Column(name = "ip_address")
    var ipAddress: String? = null,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()
