package com.pulse.scheduler

import com.pulse.domain.NotificationType
import com.pulse.repository.TaskItemRepository
import com.pulse.service.NotificationService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.temporal.ChronoUnit

/**
 * Task Automation Engine (background-worker part).
 *
 * Event-based automations that react instantly (notify on mention, notify
 * assignee, notify project on document change) are wired directly into the
 * relevant service call (see MessageService.notifyMentions, TaskService,
 * DocumentService) since they have no "wait" component. Time-based rules
 * that must poll - like "remind one day before the deadline" - run here as
 * a scheduled background worker, independent of any single request.
 */
@Component
class DeadlineReminderJob(
    private val taskItemRepository: TaskItemRepository,
    private val notificationService: NotificationService
) {

    @Scheduled(fixedRate = 15 * 60 * 1000) // every 15 minutes
    fun remindUpcomingDeadlines() {
        val now = Instant.now()
        val in24h = now.plus(24, ChronoUnit.HOURS)
        val dueSoon = taskItemRepository.findUpcomingDeadlines(now, in24h)
        dueSoon.forEach { task ->
            val assignee = task.assigneeId ?: return@forEach
            notificationService.create(
                assignee,
                NotificationType.TASK_DEADLINE,
                "Deadline approaching: ${task.title}",
                "Due ${task.dueDate}",
                null
            )
        }
    }
}
