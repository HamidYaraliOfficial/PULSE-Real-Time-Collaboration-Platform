package com.pulse.security

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenProvider(
    @Value("\${pulse.jwt.secret}") private val secret: String,
    @Value("\${pulse.jwt.access-token-ttl-minutes}") private val accessTtlMinutes: Long
) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun generateAccessToken(userId: UUID, email: String): String {
        val now = Date()
        val expiry = Date(now.time + accessTtlMinutes * 60_000)
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(key)
            .compact()
    }

    fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload

    fun getUserId(token: String): UUID = UUID.fromString(parseClaims(token).subject)

    fun isValid(token: String): Boolean = try {
        parseClaims(token)
        true
    } catch (ex: Exception) {
        false
    }
}
