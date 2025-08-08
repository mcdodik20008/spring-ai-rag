package mcdodik.springai.config.advisors

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mcdodik.advisors.vector-advisor")
data class VectorAdvisorProperties(
    val topK: Int = 15,
    /** Порог на стороне VectorStore: similarity >= value */
    val vectorStoreSimilarityThreshold: Double = 0.0,  // низкий, чтобы не потерять кандидатов
    /** Клиентский порог после пересчёта similarity вручную */
    val rerankSimilarityThreshold: Double = 0.75,
    /** Сколько документов уйдёт в итоговый контекст после переранка */
    val finalK: Int = 10,
    /** Максимум токенов под контекст (позже применим) */
    val maxContextTokens: Int = 3500,
    /** Порядок Advisor’а */
    val order: Int = 0
)
