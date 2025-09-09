package mcdodik.springai.advisors.config

import mcdodik.springai.infrastructure.document.cofig.ChatModelTemplates.GENERATE_CHUNKING_PROMPT_PROMPT
import mcdodik.springai.infrastructure.document.cofig.ChatModelTemplates.RAG_PROMPT_TEMPLATE

object ChatModelPrompts {
    fun generateChunkingPrompt(
        domain: String,
        description: String,
    ): String =
        GENERATE_CHUNKING_PROMPT_PROMPT
            .replace("{domain_name}", domain)
            .replace("{user_description}", description)

    fun ragPrompt(
        docSummary: String,
        context: String,
        question: String,
    ): String =
        RAG_PROMPT_TEMPLATE
            .replace("{doc_summary}", docSummary)
            .replace("{context}", context)
            .replace("{question}", question)
}
