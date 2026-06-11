package com.moodboard.backend.controller

import com.moodboard.backend.dto.ImageResponse
import com.moodboard.backend.model.Image
import com.moodboard.backend.service.ImageService
import com.moodboard.backend.storage.StorageService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class ImageController(
    private val imageService: ImageService,
    private val storageService: StorageService,   // used only to build URLs for responses
) {

    /**
     * POST /upload — receives a file + category over HTTP, hands the raw bytes
     * down to the service, returns the created image as JSON.
     * MultipartFile is an HTTP concept and lives ONLY here.
     */
    @PostMapping("/upload")
    @ResponseStatus(HttpStatus.CREATED)   // 201 — a new resource was created
    fun upload(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("categoryId") categoryId: Long,
    ): ImageResponse {
        if (file.isEmpty) {
            throw IllegalArgumentException("Uploaded file is empty")
        }

        val image = imageService.upload(
            content = file.bytes,                  // pull raw bytes out of the HTTP wrapper
            contentType = file.contentType ?: "application/octet-stream",
            originalFilename = file.originalFilename,
            categoryId = categoryId,
        )
        return image.toResponse()
    }

    /** GET /images/latest — the endpoint the mobile app hits on open. */
    @GetMapping("/images/latest")
    fun latest(
        @RequestParam(defaultValue = "20") limit: Int,
    ): List<ImageResponse> =
        imageService.latest(limit).map { it.toResponse() }

    /** GET /images — list all, or filter by ?categoryId=. */
    @GetMapping("/images")
    fun list(
        @RequestParam(required = false) categoryId: Long?,
        @RequestParam(defaultValue = "50") limit: Int,
    ): List<ImageResponse> =
        imageService.list(categoryId, limit).map { it.toResponse() }

    /** Internal → public translation: Image model becomes ImageResponse DTO. */
    private fun Image.toResponse() = ImageResponse(
        id = id,
        url = storageService.urlFor(storageKey),   // key → usable URL, here
        categoryId = categoryId,
        contentType = contentType,
        width = width,
        height = height,
        createdAt = createdAt,
    )
}