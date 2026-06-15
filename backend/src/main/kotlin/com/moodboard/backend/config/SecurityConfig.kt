package com.moodboard.backend.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import com.moodboard.backend.config.JwtAuthFilter
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // We're a JSON API, not a form-based website, so disable CSRF
            // (a protection meant for browser form submissions, not token APIs).
            .csrf { it.disable() }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            // No server-side sessions — every request proves itself with a token.
            // This is what "stateless" means: the server remembers nothing between calls.
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            // THE ACTUAL RULES — who can hit what:
            .authorizeHttpRequests {
                it
                    // Anyone can log in (no token needed to GET a token, obviously).
                    .requestMatchers("/auth/login").permitAll()
                    // Anyone can read images — public, for the mobile app.
                    .requestMatchers(HttpMethod.GET, "/images", "/images/latest").permitAll()
                    // Health check stays open.
                    .requestMatchers("/health").permitAll()
                    // Everything else (notably POST /upload) requires being logged in.
                    .anyRequest().authenticated()
            }
            // Plug our JWT filter in BEFORE the default auth filter, so tokens
            // get checked on the way in.
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    // The tool that hashes & checks passwords. Must be BCrypt to match the
    // hashes we generated and stored. Spring uses this when verifying logins.
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}