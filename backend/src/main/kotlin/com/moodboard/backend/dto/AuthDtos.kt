package com.moodboard.backend.dto

/** What the client sends to log in. */
data class LoginRequest(
    val email: String,
    val password: String,
)

/** What we send back on successful login: the token. */
data class LoginResponse(
    val token: String,
)