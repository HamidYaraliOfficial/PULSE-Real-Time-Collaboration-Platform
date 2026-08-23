package com.pulse.controller

import com.pulse.dto.NotificationResponse
import com.pulse.service.NotificationService
import com.pulse.util.currentUserId
import org.springframework.web.bind.annotation.*
import java.util.UUID

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(private val notificationService: NotificationService) {

    @GetMapping
    fun list(): List<NotificationResponse> = notificationService.list(currentUserId())

    @GetMapping("/unread-count")
    fun unreadCount(): Map<String, Long> = mapOf("count" to notificationService.unreadCount(currentUserId()))

    @PostMapping("/{id}/read")
    fun markRead(@PathVariable id: UUID) = notificationService.markRead(id, currentUserId())

    @PostMapping("/read-all")
    fun markAllRead() = notificationService.markAllRead(currentUserId())
}
