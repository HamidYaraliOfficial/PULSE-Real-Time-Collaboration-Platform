package com.pulse.service

import com.pulse.domain.PresenceStatus
import com.pulse.domain.ProjectStatus
import com.pulse.dto.DashboardSummaryResponse
import com.pulse.repository.*
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class DashboardService(
    private val taskItemRepository: TaskItemRepository,
    private val channelMemberRepository: ChannelMemberRepository,
    private val messageRepository: MessageRepository,
    private val notificationRepository: NotificationRepository,
    private val eventAttendeeRepository: EventAttendeeRepository,
    private val calendarEventRepository: CalendarEventRepository,
    private val projectRepository: ProjectRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val presenceService: PresenceService
) {

    fun summarize(workspaceId: UUID, userId: UUID): DashboardSummaryResponse {
        val myTaskCount = taskItemRepository.findAllByAssigneeId(userId).count { it.status.name != "DONE" }

        val myChannels = channelMemberRepository.findAllByUserId(userId)
        val unreadMessages = myChannels.sumOf { member ->
            val messages = messageRepository.findAllByChannelIdAndIsDeletedFalseOrderByCreatedAtDesc(member.channelId)
            if (member.lastReadAt != null) messages.count { it.createdAt.isAfter(member.lastReadAt) } else messages.size
        }

        val unreadNotifications = notificationRepository.countByUserIdAndIsReadFalse(userId).toInt()

        val myEventIds = eventAttendeeRepository.findAllByUserId(userId).map { it.eventId }.toSet()
        val now = java.time.Instant.now()
        val upcomingMeetings = myEventIds.mapNotNull { calendarEventRepository.findById(it).orElse(null) }
            .count { it.startsAt.isAfter(now) }

        val activeProjects = projectRepository.findAllByWorkspaceId(workspaceId).count { it.status == ProjectStatus.ACTIVE }

        val onlineMembers = workspaceMemberRepository.findAllByWorkspaceId(workspaceId)
            .count { presenceService.getStatus(it.userId) != PresenceStatus.OFFLINE }

        return DashboardSummaryResponse(
            myTaskCount = myTaskCount,
            unreadMessageCount = unreadMessages,
            unreadNotificationCount = unreadNotifications,
            upcomingMeetingCount = upcomingMeetings,
            activeProjectCount = activeProjects,
            onlineMemberCount = onlineMembers
        )
    }
}
