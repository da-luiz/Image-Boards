package com.moodboard.backend.storage

/**
 * The contract for storing image files. The rest of the app depends ONLY on
 * this interface — it never knows whether files live on local disk or in R2.
 */
interface StorageService {
    /**
     * Stores the given file content and returns its storage key — a stable
     * identifier for the stored object (e.g. "9f2c8a....jpg"). We save this
     * key in the database, never a full URL.
     */
    fun store(content: ByteArray, contentType: String, originalFilename: String?): String
    /** Builds a retrievable URL for a previously stored key. */
    fun urlFor(storageKey: String): String
}