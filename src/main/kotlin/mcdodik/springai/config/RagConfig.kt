package mcdodik.springai.config

import mcdodik.springai.db.mybatis.mapper.Bm25Mapper
import mcdodik.springai.db.mybatis.mapper.DocumentInfoMapper
import mcdodik.springai.rag.api.Retriever
import mcdodik.springai.rag.application.DbSummaryService
import mcdodik.springai.rag.application.DefaultReranker
import mcdodik.springai.rag.application.HybridRetriever
import mcdodik.springai.rag.application.MarkdownContextBuilder
import mcdodik.springai.rag.application.PostgresBm25Retriever
import mcdodik.springai.rag.application.VectorStoreRetriever
import mcdodik.springai.rag.config.HybridConfig
import mcdodik.springai.rag.model.FuseMode
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration("ragCoreConfig")
class RagConfig {
    @Bean("vectorRetriever")
    fun retriever(
        @Qualifier("customPgVectorStore") vectorStore: VectorStore,
    ) = VectorStoreRetriever(vectorStore)

    @Bean
    fun reranker() = DefaultReranker()

    @Bean
    fun contextBuilder() = MarkdownContextBuilder()

    @Bean
    fun summaryService(mapper: DocumentInfoMapper) = DbSummaryService(mapper)

    @Bean("bm25Retriever")
    fun bm25Retriever(mapper: Bm25Mapper): Retriever = PostgresBm25Retriever(mapper)

    @Primary
    @Bean("hybridRetriever")
    fun hybridRetriever(
        @Qualifier("vectorRetriever") vectorRetriever: Retriever,
        @Qualifier("bm25Retriever") bm25Retriever: Retriever,
    ): Retriever =
        HybridRetriever(
            vector = vectorRetriever,
            bm25 = bm25Retriever,
            cfg =
                HybridConfig(
                    vecWeight = 0.5,
                    bmWeight = 0.5,
                    vecTopK = 30,
                    bmTopK = 30,
                    finalTopK = 20,
                    mode = FuseMode.RRF,
                    rrfK = 60,
                ),
        )

    @Bean
    fun tokenTextSplitter(): TokenTextSplitter =
        TokenTextSplitter(
            CHUNK_SIZE,
            MIN_CHUNK_SIZE_CHARS,
            MIN_CHUNK_LENGTH_TO_EMBED,
            MAX_NUM_CHUNKS,
            true,
        )

    companion object {
        const val CHUNK_SIZE = 1000
        const val MIN_CHUNK_SIZE_CHARS = 256
        const val MIN_CHUNK_LENGTH_TO_EMBED = 128
        const val MAX_NUM_CHUNKS = 1000
    }
}
