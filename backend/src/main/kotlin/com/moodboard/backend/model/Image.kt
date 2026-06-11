package com.moodboard.backend.model

import java.time.Instant
/**
 * The in-app representation of one image's metadata — mirrors a row in the
 * `images` table. This is the neutral "language" the layers pass between each
 * other: the repository builds it from a DB row, the service hands it around,
 * the controller turns it into a response.
 *
 * Note what is NOT here: no HTTP types, no SQL types. Just plain data. That
 * neutrality is what lets it travel across every layer without coupling them.
 */
data class Image(
    val id: Long,
    val storageKey: String,
    val categoryId: Long,
    val uploadedBy: Long?,      // nullable — becomes a real FK to admins in 2.5
    val contentType: String,
    val sizeBytes: Long,
    val width: Int?,
    val height: Int?,
    val createdAt: Instant,
)