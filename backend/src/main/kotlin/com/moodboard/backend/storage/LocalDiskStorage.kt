package com.moodboard.backend.storage

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.file.Files
import java.nio.file.Path
import java.util.UUID   

@Service
class LocalDiskStorage(
    @Value("\${storage.local.directory:uploads}")
    private val directory: String,
) : StorageService {

    private val basePath: Path = Path.of(directory)

    init {
        // Make sure the uploads folder exists when the app starts.
        Files.createDirectories(basePath)
    }

    override fun store(content: ByteArray, contentType: String, originalFilename: String?): String {
        // Build a unique key so two uploads with the same filename don't collide.
        val extension = originalFilename
            ?.substringAfterLast('.', "")
            ?.takeIf { it.isNotBlank() }
        val key = if (extension != null) "${UUID.randomUUID()}.$extension"
        else UUID.randomUUID().toString()

        Files.write(basePath.resolve(key), content)
        return key
    }

    override fun urlFor(storageKey: String): String =
        "http://localhost:8080/files/$storageKey"
}