package mcdodik.springai.config

import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.rag.impl.DbSummaryService
import mcdodik.springai.rag.impl.DefaultReranker
import mcdodik.springai.rag.impl.MarkdownContextBuilder
import mcdodik.springai.rag.impl.VectorStoreRetriever
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RagConfig {
    @Bean fun retriever(@Qualifier("customPgVectorStore") vectorStore: VectorStore) = VectorStoreRetriever(vectorStore)
    @Bean fun reranker(embeddingModel: OllamaEmbeddingModel) = DefaultReranker(embeddingModel)
    @Bean fun contextBuilder() = MarkdownContextBuilder()
    @Bean fun summaryService(mapper: DocumentInfoMapper) = DbSummaryService(mapper)
}
