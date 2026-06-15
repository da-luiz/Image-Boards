package com.moodboard.backend.controller

import com.moodboard.backend.config.JwtService
import com.moodboard.backend.dto.LoginRequest
import com.moodboard.backend.dto.LoginResponse
import com.moodboard.backend.repository.AdminRepository
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.server.ResponseStatusException

@RestController
class AuthController(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtService: JwtService,
) {

    @PostMapping("/auth/login")
    fun login(@RequestBody request: LoginRequest): LoginResponse {
        // 1. Find the admin by the email they typed.
        val admin = adminRepository.findByEmail(request.email)
            ?: throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")

        // 2. Check the typed password against the stored bcrypt hash.
        //    passwordEncoder.matches() hashes the typed password the same way
        //    and compares — it never un-hashes the stored one.
        if (!passwordEncoder.matches(request.password, admin.passwordHash)) {
            throw ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password")
        }

        // 3. Password is correct → issue a JWT (the wristband).
        val token = jwtService.createToken(admin.id, admin.email)
        return LoginResponse(token)
    }
}