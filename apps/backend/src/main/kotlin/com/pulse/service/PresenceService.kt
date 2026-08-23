package com.pulse.service

import com.pulse.domain.PresenceStatus
import com.pulse.dto.PresenceEvent
import com.pulse.repository.UserRepository
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.UUID

/**
 * Presence is kept in Redis (key: "presence:{userId}") with a TTL so that a
 * client that disconnects without a clean close still falls back to OFFLINE
 * automatically. Every change is broadcast over STOMP so all workspace
 * members see live presence dots without polling. Using Redis (instead of an
 * in-memory map) is what lets this scale horizontally across multiple
 * backend instances behind a load balancer.
 */
@Service
class PresenceService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val messagingTemplate: SimpMessagingTemplate,
    private val userRepository: UserRepository
) {
    private val heartbeatTtl = Duration.ofSeconds(90)

    fun setStatus(userId: UUID, status: PresenceStatus) {
        redisTemplate.opsForValue().set("presence:$userId", status.name, heartbeatTtl)
        userRepository.findById(userId).ifPresent {
            it.presenceStatus = status
            it.lastActiveAt = Instant.now()
            userRepository.save(it)
        }
        broadcast(userId, status)
    }

    fun heartbeat(userId: UUID) {
        val current = getStatus(userId)
        redisTemplate.expire("presence:$userId", heartbeatTtl)
        if (current == PresenceStatus.OFFLINE) {
            setStatus(userId, PresenceStatus.ONLINE)
        }
    }

    fun getStatus(userId: UUID): PresenceStatus {
        val raw = redisTemplate.opsForValue().get("presence:$userId") as String?
        return raw?.let { PresenceStatus.valueOf(it) } ?: PresenceStatus.OFFLINE
    }

    fun markOffline(userId: UUID) = setStatus(userId, PresenceStatus.OFFLINE)

    private fun broadcast(userId: UUID, status: PresenceStatus) {
        messagingTemplate.convertAndSend(
            "/topic/presence",
            PresenceEvent(userId = userId, status = status.name, lastActiveAt = Instant.now())
        )
    }
}
