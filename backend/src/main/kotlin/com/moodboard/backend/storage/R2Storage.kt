package com.moodboard.backend.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Service
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

/**
 * Stores files in Cloudflare R2 instead of local disk.
 * Fulfills the SAME StorageService contract as LocalDiskStorage — so nothing
 * above this (service, controller) changes. @Primary tells Spring to use THIS
 * implementation when something asks for a StorageService.
 */
@Service
@Primary
class R2Storage(
    private val s3Client: S3Client,
    @Value("\${r2.bucket-name}") private val bucketName: String,
    @Value("\${r2.public-url}") private val publicUrl: String,
) : StorageService {

    override fun store(content: ByteArray, contentType: String, originalFilename: String?): String {
        // Build a unique key, same idea as the disk version.
        val extension = originalFilename
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
        val key = if (extension != null) "${UUID.randomUUID()}.$extension"
        else UUID.randomUUID().toString()

        // Upload the bytes to R2.
        val request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build()

        s3Client.putObject(request, RequestBody.fromBytes(content))

        return key
    }

    /** Build the public URL for a stored object. With a public bucket, this URL works directly. */
    override fun urlFor(storageKey: String): String =
        "$publicUrl/$storageKey"
}