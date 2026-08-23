package com.pulse.service

import com.pulse.domain.User
import com.pulse.dto.LoginRequest
import com.pulse.exception.ApiException
import com.pulse.repository.*
import com.pulse.security.JwtTokenProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import java.util.Optional
import java.util.UUID

class AuthServiceTest {

    private val userRepository = mockk<UserRepository>()
    private val refreshTokenRepository = mockk<RefreshTokenRepository>(relaxed = true)
    private val organizationRepository = mockk<OrganizationRepository>(relaxed = true)
    private val workspaceRepository = mockk<WorkspaceRepository>(relaxed = true)
    private val workspaceMemberRepository = mockk<WorkspaceMemberRepository>(relaxed = true)
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val passwordEncoder = BCryptPasswordEncoder(4)
    private val auditLogService = mockk<AuditLogService>(relaxed = true)

    private val service = AuthService(
        userRepository, refreshTokenRepository, organizationRepository, workspaceRepository,
        workspaceMemberRepository, jwtTokenProvider, passwordEncoder, auditLogService
    )

    @Test
    fun `login fails with wrong password`() {
        val user = User(email = "a@b.com", passwordHash = passwordEncoder.encode("correct-password"), displayName = "A")
        user.id = UUID.randomUUID()
        every { userRepository.findByEmail("a@b.com") } returns user

        val ex = assertThrows(ApiException::class.java) {
            service.login(LoginRequest(email = "a@b.com", password = "wrong-password"))
        }
        assert(ex.status == 401)
    }

    @Test
    fun `login fails for unknown email`() {
        every { userRepository.findByEmail("nobody@nowhere.com") } returns null

        assertThrows(ApiException::class.java) {
            service.login(LoginRequest(email = "nobody@nowhere.com", password = "whatever"))
        }
    }
}
