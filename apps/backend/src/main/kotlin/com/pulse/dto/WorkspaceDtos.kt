package com.pulse.dto

import jakarta.validation.constraints.NotBlank
import java.time.Instant
import java.util.UUID

data class CreateWorkspaceRequest(
    @field:NotBlank val name: String,
    val description: String? = null,
    val timezone: String = "UTC"
)

data class WorkspaceResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val description: String?,
    val logoUrl: String?,
    val timezone: String,
    val role: String,
    val memberCount: Int
)

data class InviteMemberRequest(
    @field:NotBlank val email: String,
    val role: String = "MEMBER"
)

data class WorkspaceMemberResponse(
    val userId: UUID,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val presenceStatus: String,
    val title: String?
)

data class CreateTeamRequest(@field:NotBlank val name: String, val description: String? = null)

data class BusinessHourEntry(
    val dayOfWeek: Int,
    val isClosed: Boolean,
    val openTime: String?,   // "09:00"
    val closeTime: String?,  // "18:00"
    val timezone: String = "UTC"
)

data class BusinessHoursUpdateRequest(val days: List<BusinessHourEntry>)

data class BusinessHoursStatusResponse(
    val isOpenNow: Boolean,
    val timezone: String,
    val currentLocalTime: String,
    val nextChangeAt: Instant?,
    val nextChangeType: String, // "OPENS" | "CLOSES"
    val secondsUntilNextChange: Long?,
    val schedule: List<BusinessHourEntry>
)
