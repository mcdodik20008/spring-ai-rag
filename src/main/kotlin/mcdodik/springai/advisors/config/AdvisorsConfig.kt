package mcdodik.springai.advisors.config

import mcdodik.springai.advisors.HybridAdvisor
import mcdodik.springai.advisors.PostRequestAdvisor
import mcdodik.springai.advisors.RussianAdvisor
import mcdodik.springai.rag.service.api.ContextBuilder
import mcdodik.springai.rag.service.api.Reranker
import mcdodik.springai.rag.service.api.Retriever
import mcdodik.springai.rag.service.api.SummaryService
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

    @Bean
    fun postRequestAdvisor() = PostRequestAdvisor()
}
