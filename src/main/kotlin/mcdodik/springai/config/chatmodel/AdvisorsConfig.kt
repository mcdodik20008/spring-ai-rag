package mcdodik.springai.config.chatmodel

import mcdodik.springai.advisors.HybridAdvisor
import mcdodik.springai.advisors.RussianAdvisor
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.api.SummaryService
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AdvisorsConfig {
    @Bean
    fun hybridAdvisor(
        props: VectorAdvisorProperties,
        embeddingModel: OllamaEmbeddingModel,
        hybridRetriever: Retriever,
        reranker: Reranker,
        contextBuilder: ContextBuilder,
        summaryService: SummaryService,
    ) = HybridAdvisor(props, embeddingModel, hybridRetriever, reranker, contextBuilder, summaryService)

    @Bean
    fun russianAdvisor() = RussianAdvisor()
}
