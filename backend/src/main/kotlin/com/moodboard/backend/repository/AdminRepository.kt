package com.moodboard.backend.repository

import com.moodboard.backend.model.Admin
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet

@Repository
class AdminRepository(
    private val jdbcClient: JdbcClient,
) {

    /**
     * Find one admin by email. Returns null if no admin with that email exists.
     * We look up by email because that's what the user types when logging in.
     */
    fun findByEmail(email: String): Admin? =
        jdbcClient.sql(
            """
            SELECT id, email, password_hash, created_at
            FROM admins
            WHERE email = :email
            """.trimIndent()
        )
            .param("email", email)
            .query { rs, _ -> mapRowToAdmin(rs) }
            .optional()              // returns Optional — empty if no match
            .orElse(null)            // convert to Kotlin null if empty

    private fun mapRowToAdmin(rs: ResultSet): Admin =
        Admin(
            id = rs.getLong("id"),
            email = rs.getString("email"),
            passwordHash = rs.getString("password_hash"),
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )
}