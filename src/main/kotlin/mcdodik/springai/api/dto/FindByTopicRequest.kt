package mcdodik.springai.api.dto

/** Request DTO for topic search. */
data class FindByTopicRequest(
    val topic: String,
    val k: Int = 5,
    val minSim: Double = 0.35,
)
