package com.moodboard.backend.config

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.util.Date

/**
 * Creates and verifies JWTs (the "wristband").
 * - createToken: called after a successful login, to issue a token.
 * - verifyToken: called on protected requests, to check a token is valid.
 *
 * The token is SIGNED with a secret only this server knows. Anyone can read a
 * JWT, but only someone with the secret can create a valid one — that's what
 * makes it un-fakeable.
 */
@Service
class JwtService(
    @Value("\${app.jwt.secret}")
    private val secret: String,
    @Value("\${app.jwt.expiration-ms:86400000}")   // default: 24 hours
    private val expirationMs: Long,
) {

    private val algorithm: Algorithm by lazy { Algorithm.HMAC256(secret) }

    /** Issue a token for a logged-in admin. We put the admin's id and email inside it. */
    fun createToken(adminId: Long, email: String): String {
        val now = Date()
        val expiry = Date(now.time + expirationMs)

        return JWT.create()
            .withSubject(adminId.toString())   // who the token is for
            .withClaim("email", email)         // extra info we store in the token
            .withIssuedAt(now)
            .withExpiresAt(expiry)             // token stops being valid after this
            .sign(algorithm)                   // sign it with our secret
    }

    /**
     * Verify a token. Returns the admin id if valid, or null if invalid/expired.
     * "Valid" means: correctly signed by us AND not expired.
     */
    fun verifyToken(token: String): Long? =
        try {
            val decoded = JWT.require(algorithm).build().verify(token)
            decoded.subject.toLong()           // the adminId we stored
        } catch (e: JWTVerificationException) {
            null                                // bad signature, expired, or malformed
        }
}