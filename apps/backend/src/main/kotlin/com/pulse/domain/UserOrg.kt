package com.pulse.domain

import jakarta.persistence.*
import java.time.Instant
import java.time.LocalTime
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity {
    @Id
    @GeneratedValue
    var id: UUID? = null
}

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    var email: String,

    @Column(name = "password_hash", nullable = false)
    var passwordHash: String,

    @Column(name = "display_name", nullable = false)
    var displayName: String,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    var title: String? = null,

    var timezone: String = "UTC",

    var locale: String = "en",

    @Enumerated(EnumType.STRING)
    @Column(name = "presence_status")
    var presenceStatus: PresenceStatus = PresenceStatus.OFFLINE,

    @Column(name = "last_active_at")
    var lastActiveAt: Instant? = null,

    @Column(name = "email_verified")
    var emailVerified: Boolean = false,

    @Column(name = "two_factor_enabled")
    var twoFactorEnabled: Boolean = false,

    @Column(name = "two_factor_secret")
    var twoFactorSecret: String? = null,

    @Column(name = "is_active")
    var isActive: Boolean = true,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "organizations")
class Organization(
    var name: String,
    var slug: String,

    @Column(name = "owner_id")
    var ownerId: UUID,

    @Column(name = "logo_url")
    var logoUrl: String? = null,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "workspaces")
class Workspace(
    @Column(name = "organization_id")
    var organizationId: UUID,

    var name: String,
    var slug: String,
    var description: String? = null,

    @Column(name = "logo_url")
    var logoUrl: String? = null,

    var timezone: String = "UTC",

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "workspace_members")
class WorkspaceMember(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    var role: WorkspaceRole = WorkspaceRole.MEMBER,

    @Column(name = "joined_at")
    var joinedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "teams")
class Team(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    var name: String,
    var description: String? = null,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
) : BaseEntity()

@Entity
@Table(name = "team_members")
class TeamMember(
    @Column(name = "team_id")
    var teamId: UUID,

    @Column(name = "user_id")
    var userId: UUID,

    @Enumerated(EnumType.STRING)
    var role: WorkspaceRole = WorkspaceRole.MEMBER
) : BaseEntity()

@Entity
@Table(name = "business_hours")
class BusinessHours(
    @Column(name = "workspace_id")
    var workspaceId: UUID,

    @Column(name = "day_of_week")
    var dayOfWeek: Int, // 0 = Sunday .. 6 = Saturday

    @Column(name = "is_closed")
    var isClosed: Boolean = false,

    @Column(name = "open_time")
    var openTime: LocalTime? = null,

    @Column(name = "close_time")
    var closeTime: LocalTime? = null,

    var timezone: String = "UTC"
) : BaseEntity()

@Entity
@Table(name = "refresh_tokens")
class RefreshToken(
    @Column(name = "user_id")
    var userId: UUID,

    @Column(name = "token_hash", unique = true)
    var tokenHash: String,

    @Column(name = "device_name")
    var deviceName: String? = null,

    @Column(name = "device_ip")
    var deviceIp: String? = null,

    @Column(name = "expires_at")
    var expiresAt: Instant,

    var revoked: Boolean = false,

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
) : BaseEntity()
