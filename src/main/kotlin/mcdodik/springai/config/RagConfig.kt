package mcdodik.springai.config

import mcdodik.springai.advisors.HybridAdvisor
import mcdodik.springai.config.advisors.VectorAdvisorProperties
import mcdodik.springai.db.mybatis.mapper.Bm25Mapper
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.rag.api.ContextBuilder
import mcdodik.springai.rag.api.Reranker
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.api.SummaryService
import mcdodik.springai.rag.application.DbSummaryService
import mcdodik.springai.rag.application.DefaultReranker
import mcdodik.springai.rag.application.HybridRetriever
import mcdodik.springai.rag.application.MarkdownContextBuilder
import mcdodik.springai.rag.application.PostgresBm25Retriever
import mcdodik.springai.rag.application.VectorStoreRetriever
import mcdodik.springai.rag.model.FuseMode
import mcdodik.springai.rag.model.HybridConfig
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration("ragCoreConfig")
class RagConfig {

    @Bean("vectorRetriever")
    fun retriever(@Qualifier("customPgVectorStore") vectorStore: VectorStore) =
        VectorStoreRetriever(vectorStore)

    @Bean
    fun reranker() = DefaultReranker()

    @Bean
    fun contextBuilder() = MarkdownContextBuilder()

    @Bean
    fun summaryService(mapper: DocumentInfoMapper) = DbSummaryService(mapper)

    @Bean("bm25Retriever")
    fun bm25Retriever(mapper: Bm25Mapper): Retriever =
        PostgresBm25Retriever(mapper)

    @Primary
    @Bean("hybridRetriever")
    fun hybridRetriever(
        @Qualifier("vectorRetriever") vectorRetriever: Retriever,
        @Qualifier("bm25Retriever") bm25Retriever: Retriever
    ): Retriever = HybridRetriever(
        vector = vectorRetriever,
        bm25 = bm25Retriever,
        cfg = HybridConfig(
            vecWeight = 0.5,
            bmWeight = 0.5,
            vecTopK = 30,
            bmTopK = 30,
            finalTopK = 20,
            mode = FuseMode.RRF,
            rrfK = 60
        )
    )

    @Bean
    fun hybridAdvisor(
        props: VectorAdvisorProperties,
        embeddingModel: OllamaEmbeddingModel,
        hybridRetriever: Retriever,
        reranker: Reranker,
        contextBuilder: ContextBuilder,
        summaryService: SummaryService
    ) = HybridAdvisor(props, embeddingModel, hybridRetriever, reranker, contextBuilder, summaryService)

    @Bean
    fun tokenTextSplitter(): TokenTextSplitter = TokenTextSplitter(
        1000,     // chunkSize
        256,      // minChunkSizeChars
        128,      // minChunkLengthToEmbed
        1000,     // maxNumChunks
        true      // keepSeparator
    )
}