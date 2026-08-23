package com.pulse.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.UUID

data class RegisterRequest(
    @field:Email val email: String,
    @field:Size(min = 8, message = "Password must be at least 8 characters") val password: String,
    @field:NotBlank val displayName: String
)

data class LoginRequest(
    @field:Email val email: String,
    @field:NotBlank val password: String,
    val deviceName: String? = null
)

data class RefreshRequest(@field:NotBlank val refreshToken: String)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserSummary
)

data class UserSummary(
    val id: UUID,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
    val title: String?,
    val presenceStatus: String,
    val locale: String
)

data class UpdateProfileRequest(
    val displayName: String? = null,
    val title: String? = null,
    val avatarUrl: String? = null,
    val timezone: String? = null,
    val locale: String? = null
)

data class ChangePasswordRequest(
    @field:NotBlank val currentPassword: String,
    @field:Size(min = 8) val newPassword: String
)
