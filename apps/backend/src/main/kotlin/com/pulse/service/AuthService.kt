package com.pulse.service

import com.pulse.domain.Organization
import com.pulse.domain.User
import com.pulse.domain.Workspace
import com.pulse.domain.WorkspaceMember
import com.pulse.domain.WorkspaceRole
import com.pulse.dto.*
import com.pulse.exception.ApiException
import com.pulse.repository.*
import com.pulse.security.JwtTokenProvider
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val organizationRepository: OrganizationRepository,
    private val workspaceRepository: WorkspaceRepository,
    private val workspaceMemberRepository: WorkspaceMemberRepository,
    private val jwtTokenProvider: JwtTokenProvider,
    private val passwordEncoder: BCryptPasswordEncoder,
    private val auditLogService: AuditLogService
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw ApiException(409, "An account with this email already exists")
        }
        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password),
            displayName = request.displayName
        )
        userRepository.save(user)

        // Every new user gets a personal organization + default workspace,
        // mirroring how Slack/Notion bootstrap a first workspace on sign-up.
        val org = Organization(
            name = "${request.displayName}'s Organization",
            slug = "org-${UUID.randomUUID().toString().take(8)}",
            ownerId = user.id!!
        )
        organizationRepository.save(org)

        val workspace = Workspace(
            organizationId = org.id!!,
            name = "My Workspace",
            slug = "workspace-${UUID.randomUUID().toString().take(8)}"
        )
        workspaceRepository.save(workspace)

        workspaceMemberRepository.save(
            WorkspaceMember(workspaceId = workspace.id!!, userId = user.id!!, role = WorkspaceRole.OWNER)
        )

        auditLogService.log(workspace.id, user.id, "USER_REGISTERED", "USER", user.id)

        return issueTokens(user, null)
    }

    @Transactional
    fun login(request: LoginRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw ApiException(401, "Invalid email or password")
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw ApiException(401, "Invalid email or password")
        }
        user.lastActiveAt = Instant.now()
        userRepository.save(user)
        return issueTokens(user, request.deviceName)
    }

    @Transactional
    fun refresh(request: RefreshRequest): AuthResponse {
        val hash = sha256(request.refreshToken)
        val stored = refreshTokenRepository.findByTokenHash(hash)
            ?: throw ApiException(401, "Invalid refresh token")
        if (stored.revoked || stored.expiresAt.isBefore(Instant.now())) {
            throw ApiException(401, "Refresh token expired")
        }
        val user = userRepository.findById(stored.userId).orElseThrow { ApiException(404, "User not found") }
        stored.revoked = true
        refreshTokenRepository.save(stored)
        return issueTokens(user, stored.deviceName)
    }

    @Transactional
    fun logout(refreshToken: String) {
        val hash = sha256(refreshToken)
        refreshTokenRepository.findByTokenHash(hash)?.let {
            it.revoked = true
            refreshTokenRepository.save(it)
        }
    }

    private fun issueTokens(user: User, deviceName: String?): AuthResponse {
        val accessToken = jwtTokenProvider.generateAccessToken(user.id!!, user.email)
        val rawRefreshToken = Base64.getUrlEncoder().withoutPadding().encodeToString(UUID.randomUUID().toString().toByteArray())
        refreshTokenRepository.save(
            com.pulse.domain.RefreshToken(
                userId = user.id!!,
                tokenHash = sha256(rawRefreshToken),
                deviceName = deviceName,
                expiresAt = Instant.now().plus(30, ChronoUnit.DAYS)
            )
        )
        return AuthResponse(
            accessToken = accessToken,
            refreshToken = rawRefreshToken,
            user = UserSummary(
                id = user.id!!,
                email = user.email,
                displayName = user.displayName,
                avatarUrl = user.avatarUrl,
                title = user.title,
                presenceStatus = user.presenceStatus.name,
                locale = user.locale
            )
        )
    }

    private fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
