package com.moodboard.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import java.net.URI

@Configuration
class R2Config(
    @Value("\${r2.access-key-id}") private val accessKeyId: String,
    @Value("\${r2.secret-access-key}") private val secretAccessKey: String,
    @Value("\${r2.endpoint}") private val endpoint: String,
) {

    /**
     * Builds the S3 client pointed at Cloudflare R2.
     * R2 speaks the S3 protocol, so we use AWS's S3 client but aim it at
     * R2's endpoint instead of Amazon's. Region is "auto" for R2.
     */
    @Bean
    fun s3Client(): S3Client =
        S3Client.builder()
            .endpointOverride(URI.create(endpoint))   // point at R2, not AWS
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                )
            )
            .region(Region.of("auto"))                 // R2 uses "auto"
            .build()
}