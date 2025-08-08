package mcdodik.springai.db.entity.prompt

data class PromptGenerationRequest(
    val domainName: String,
    val userDescription: String
)
