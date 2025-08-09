package mcdodik.springai.config.advisors

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mcdodik.advisors.vector-advisor")
data class VectorAdvisorProperties(
    val topK: Int = 24,
    /** Порог на стороне VectorStore: similarity >= value */
    val vectorStoreSimilarityThreshold: Double = 0.35,
    /** Клиентский порог после пересчёта similarity вручную */
    val rerankSimilarityThreshold: Double = 0.75,
    /** Сколько документов уйдёт в итоговый контекст после переранка */
    val finalK: Int = 12,
    /** Максимум токенов под контекст */
    val maxContextChars: Int = 20000,
    /** Порядок Advisor’а */
    val order: Int = 0
)
