package com.moodboard.backend.model

import java.time.Instant

/**
 * In-app representation of one row in the `admins` table.
 * Note: passwordHash is the bcrypt hash, never a plain password.
 */
data class Admin(
    val id: Long,
    val email: String,
    val passwordHash: String,
    val createdAt: Instant,
)