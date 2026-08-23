package com.pulse.dto

import java.time.Instant
import java.util.UUID

data class NotificationResponse(
    val id: UUID,
    val type: String,
    val title: String,
    val body: String?,
    val link: String?,
    val isRead: Boolean,
    val createdAt: Instant
)

data class DashboardSummaryResponse(
    val myTaskCount: Int,
    val unreadMessageCount: Int,
    val unreadNotificationCount: Int,
    val upcomingMeetingCount: Int,
    val activeProjectCount: Int,
    val onlineMemberCount: Int
)
