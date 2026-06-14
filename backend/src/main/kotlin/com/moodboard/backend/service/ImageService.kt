package com.moodboard.backend.service

import com.moodboard.backend.model.Image
import com.moodboard.backend.repository.ImageRepository
import com.moodboard.backend.storage.StorageService
import org.springframework.stereotype.Service

@Service
class ImageService(
    private val storageService: StorageService,   // the INTERFACE — not disk or R2 specifically
    private val imageRepository: ImageRepository,
) {

    /**
     * The upload workflow: store the file, then save its metadata row.
     * This is the business logic — the controller (HTTP) and the repository
     * (SQL) each do their own narrow job; the service is what knows the
     * *sequence* that makes an "upload" actually mean something.
     */
    fun upload(
        content: ByteArray,
        contentType: String,
        originalFilename: String?,
        categoryId: Long,
    ): Image {
        // Step 1: hand the raw bytes to storage, get back a stable key.
        val storageKey = storageService.store(content, contentType, originalFilename)

        // Step 2: save the metadata (including that key) as a row.
        val id = imageRepository.insert(
            storageKey = storageKey,
            categoryId = categoryId,
            contentType = contentType,
            sizeBytes = content.size.toLong(),
            width = null,    // we'll extract real dimensions later; null is fine for now
            height = null,
        )

        // Step 3: hand back the freshly-created image so the controller can respond.
        return imageRepository.findById(id)
    }

    fun latest(limit: Int): List<Image> = imageRepository.findLatest(limit)

    fun list(categoryId: Long?, limit: Int): List<Image> =
        imageRepository.findAll(categoryId, limit)
}