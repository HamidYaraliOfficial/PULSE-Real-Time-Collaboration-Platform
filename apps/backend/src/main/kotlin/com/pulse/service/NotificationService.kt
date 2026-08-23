package com.pulse.service

import com.pulse.domain.NotificationEntity
import com.pulse.domain.NotificationType
import com.pulse.dto.NotificationResponse
import com.pulse.repository.NotificationRepository
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class NotificationService(
    private val repository: NotificationRepository,
    private val messagingTemplate: SimpMessagingTemplate
) {

    @Transactional
    fun create(userId: UUID, type: NotificationType, title: String, body: String?, link: String?): NotificationResponse {
        val entity = repository.save(NotificationEntity(userId = userId, type = type, title = title, body = body, link = link))
        val response = toResponse(entity)
        messagingTemplate.convertAndSendToUser(userId.toString(), "/queue/notifications", response)
        return response
    }

    fun list(userId: UUID): List<NotificationResponse> =
        repository.findAllByUserIdOrderByCreatedAtDesc(userId).map { toResponse(it) }

    fun unreadCount(userId: UUID): Long = repository.countByUserIdAndIsReadFalse(userId)

    @Transactional
    fun markRead(id: UUID, userId: UUID) {
        val notification = repository.findById(id).orElseThrow { com.pulse.exception.ApiException(404, "Notification not found") }
        if (notification.userId != userId) throw com.pulse.exception.ApiException(403, "Not your notification")
        notification.isRead = true
        repository.save(notification)
    }

    @Transactional
    fun markAllRead(userId: UUID) {
        repository.findAllByUserIdAndIsReadFalse(userId).forEach {
            it.isRead = true
            repository.save(it)
        }
    }

    private fun toResponse(n: NotificationEntity) = NotificationResponse(
        id = n.id!!, type = n.type.name, title = n.title, body = n.body, link = n.link,
        isRead = n.isRead, createdAt = n.createdAt
    )
}
