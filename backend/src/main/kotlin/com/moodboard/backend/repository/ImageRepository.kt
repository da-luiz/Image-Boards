package com.moodboard.backend.repository

import com.moodboard.backend.model.Image
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.jdbc.support.GeneratedKeyHolder
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import java.time.Instant

@Repository
class ImageRepository(
    private val jdbcClient: JdbcClient,
) {
    /**
     * Inserts one image row and returns the database-generated id.
     * We let Postgres generate the id (BIGINT GENERATED ALWAYS AS IDENTITY),
     * so we ask for the generated key back rather than supplying one.
     */
    fun insert(
        storageKey: String,
        categoryId: Long,
        contentType: String,
        sizeBytes: Long,
        width: Int?,
        height: Int?,
    ): Long {
        val keyHolder = GeneratedKeyHolder()

        jdbcClient.sql(
            """
            INSERT INTO images (storage_key, category_id, content_type, size_bytes, width, height)
            VALUES (:storageKey, :categoryId, :contentType, :sizeBytes, :width, :height)
            """.trimIndent()
        )
            .param("storageKey", storageKey)
            .param("categoryId", categoryId)
            .param("contentType", contentType)
            .param("sizeBytes", sizeBytes)
            .param("width", width)
            .param("height", height)
            .update(keyHolder, "id")

        return keyHolder.key!!.toLong()
    }

    /** Latest images overall — uses the idx_images_created_at index. */
    fun findLatest(limit: Int): List<Image> =
        jdbcClient.sql(
            """
            SELECT id, storage_key, category_id, uploaded_by, content_type,
                   size_bytes, width, height, created_at
            FROM images
            ORDER BY created_at DESC
            LIMIT :limit
            """.trimIndent()
        )
            .param("limit", limit)
            .query { rs, _ -> mapRowToImage(rs) }
            .list()

    /** All images, or just one category — uses idx_images_category_created_at when filtered. */
    fun findAll(categoryId: Long?, limit: Int): List<Image> {
        val sql = buildString {
            append(
                """
                SELECT id, storage_key, category_id, uploaded_by, content_type,
                       size_bytes, width, height, created_at
                FROM images
                """.trimIndent()
            )
            if (categoryId != null) append("\nWHERE category_id = :categoryId")
            append("\nORDER BY created_at DESC\nLIMIT :limit")
        }

        val query = jdbcClient.sql(sql).param("limit", limit)
        if (categoryId != null) query.param("categoryId", categoryId)

        return query.query { rs, _ -> mapRowToImage(rs) }.list()
    }
    /**
     * Turns one database row into an Image object. This is the bridge between
     * the DB's snake_case columns and our camelCase model — done by hand,
     * on purpose, so the mapping is visible (no ORM hiding it).
     */
    private fun mapRowToImage(rs: ResultSet): Image =
        Image(
            id = rs.getLong("id"),
            storageKey = rs.getString("storage_key"),
            categoryId = rs.getLong("category_id"),
            uploadedBy = rs.getObject("uploaded_by") as Long?,
            contentType = rs.getString("content_type"),
            sizeBytes = rs.getLong("size_bytes"),
            width = rs.getObject("width") as Int?,
            height = rs.getObject("height") as Int?,
            createdAt = rs.getTimestamp("created_at").toInstant(),
        )

    /** Fetch a single image by its id. */
    fun findById(id: Long): Image =
        jdbcClient.sql(
            """
            SELECT id, storage_key, category_id, uploaded_by, content_type,
                   size_bytes, width, height, created_at
            FROM images
            WHERE id = :id
            """.trimIndent()
        )
            .param("id", id)
            .query { rs, _ -> mapRowToImage(rs) }
            .single()
}
