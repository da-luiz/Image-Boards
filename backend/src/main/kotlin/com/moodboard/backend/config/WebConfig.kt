package com.moodboard.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.nio.file.Paths

@Configuration
class WebConfig(
    @Value("\${storage.local.directory:uploads}")
    private val directory: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // Build the URI for the uploads directory (e.g. file:/Users/.../uploads/)
        val uploadPath = Paths.get(directory).toAbsolutePath().toUri().toString()
        
        // Map any requests matching /files/** to find files in the uploads folder
        registry.addResourceHandler("/files/**")
            .addResourceLocations(uploadPath)
    }
}
