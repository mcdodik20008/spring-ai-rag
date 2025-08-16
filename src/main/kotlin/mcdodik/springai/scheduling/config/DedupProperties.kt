package mcdodik.springai.scheduling.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mcdodik.chunks.dedup")
data class DedupProperties(
    val recomputeBatchSize: Int = 2000,
    val pageSize: Int = 2000,
    val topTermsPerDoc: Int = 15,
    val candidateLimit: Int = 500,
    val similarityThreshold: Double = 0.90,
)
