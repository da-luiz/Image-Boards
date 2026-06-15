package com.moodboard.backend.config

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * Runs on every incoming request. If there's a valid "Bearer <token>" header,
 * it marks the request as authenticated so protected endpoints allow it.
 * If there's no token (or an invalid one), it just does nothing and lets
 * Spring Security reject the request later (for protected routes).
 */
@Component
class JwtAuthFilter(
    private val jwtService: JwtService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        // Look for the "Authorization: Bearer <token>" header.
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7)         // strip "Bearer "
            val adminId = jwtService.verifyToken(token) // null if invalid/expired

            if (adminId != null) {
                // Token is valid → tell Spring Security this request is authenticated.
                val authentication = UsernamePasswordAuthenticationToken(
                    adminId,        // the principal (who they are)
                    null,           // no credentials needed — token already proved it
                    emptyList(),    // no special roles for now
                )
                SecurityContextHolder.getContext().authentication = authentication
            }
        }

        // Always continue the chain — whether or not we authenticated.
        filterChain.doFilter(request, response)
    }
}