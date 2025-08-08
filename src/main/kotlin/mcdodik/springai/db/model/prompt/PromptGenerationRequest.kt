package mcdodik.springai.db.model.prompt

data class PromptGenerationRequest(
    val domainName: String,
    val userDescription: String
)
