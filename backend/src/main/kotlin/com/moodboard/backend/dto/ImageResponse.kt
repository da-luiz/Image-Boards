package com.moodboard.backend.dto

import com.moodboard.backend.model.Image
import java.time.Instant

/**
 * The PUBLIC shape of an image in API responses — deliberately separate from
 * the internal Image model. Note it exposes `url` (built from the storage key),
 * not the raw storage key, and omits internal fields the client doesn't need.
 */
data class ImageResponse(
    val id: Long,
    val url: String,
    val categoryId: Long,
    val contentType: String,
    val width: Int?,
    val height: Int?,
    val createdAt: Instant,
)