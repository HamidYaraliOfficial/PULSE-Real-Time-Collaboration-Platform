package com.pulse.security

import com.pulse.repository.UserRepository
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.stereotype.Component
import java.security.Principal

/**
 * Authenticates the STOMP CONNECT frame using the same JWT used for REST
 * calls (sent as an `Authorization: Bearer <token>` STOMP header). On
 * success the resolved user becomes the session Principal, so every
 * @MessageMapping handler and every /user/queue/* destination is scoped to
 * the right person automatically.
 */
@Component
class StompAuthChannelInterceptor(
    private val jwtTokenProvider: JwtTokenProvider,
    private val userRepository: UserRepository
) : ChannelInterceptor {

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*> {
        val accessor = StompHeaderAccessor.wrap(message)
        if (accessor.command == StompCommand.CONNECT) {
            val authHeader = accessor.getFirstNativeHeader("Authorization")
            val token = authHeader?.removePrefix("Bearer ")?.trim()
            if (token != null && jwtTokenProvider.isValid(token)) {
                val userId = jwtTokenProvider.getUserId(token)
                val user = userRepository.findById(userId).orElse(null)
                if (user != null) {
                    accessor.user = Principal { userId.toString() }
                }
            }
        }
        return message
    }
}
