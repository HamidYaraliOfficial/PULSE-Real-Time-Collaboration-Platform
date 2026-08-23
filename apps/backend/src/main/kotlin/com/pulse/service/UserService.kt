package com.pulse.service

import com.pulse.dto.UpdateProfileRequest
import com.pulse.dto.UserSummary
import com.pulse.exception.ApiException
import com.pulse.repository.UserRepository
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    fun get(userId: UUID): UserSummary {
        val user = userRepository.findById(userId).orElseThrow { ApiException(404, "User not found") }
        return UserSummary(
            id = user.id!!, email = user.email, displayName = user.displayName, avatarUrl = user.avatarUrl,
            title = user.title, presenceStatus = user.presenceStatus.name, locale = user.locale
        )
    }

    @Transactional
    fun updateProfile(userId: UUID, request: UpdateProfileRequest): UserSummary {
        val user = userRepository.findById(userId).orElseThrow { ApiException(404, "User not found") }
        request.displayName?.let { user.displayName = it }
        request.title?.let { user.title = it }
        request.avatarUrl?.let { user.avatarUrl = it }
        request.timezone?.let { user.timezone = it }
        request.locale?.let { user.locale = it }
        userRepository.save(user)
        return get(userId)
    }

    @Transactional
    fun changePassword(userId: UUID, currentPassword: String, newPassword: String) {
        val user = userRepository.findById(userId).orElseThrow { ApiException(404, "User not found") }
        if (!passwordEncoder.matches(currentPassword, user.passwordHash)) {
            throw ApiException(400, "Current password is incorrect")
        }
        user.passwordHash = passwordEncoder.encode(newPassword)
        userRepository.save(user)
    }

    fun search(query: String): List<UserSummary> =
        userRepository.findAll()
            .filter { it.displayName.contains(query, ignoreCase = true) || it.email.contains(query, ignoreCase = true) }
            .take(20)
            .map { get(it.id!!) }
}
