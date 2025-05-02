package mcdodik.springai.config

import com.knuddels.jtokkit.api.EncodingType
import org.springframework.ai.tokenizer.JTokkitTokenCountEstimator
import org.springframework.ai.tokenizer.TokenCountEstimator
import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class RagIngestConfig {

    @Bean
    fun tokenCounter(): TokenCountEstimator =
        JTokkitTokenCountEstimator(EncodingType.CL100K_BASE)

    @Bean
    fun tokenTextSplitter(counter: TokenCountEstimator): TokenTextSplitter =
        TokenTextSplitter.builder()
            .withChunkSize(300)
            .withMinChunkLengthToEmbed(100)
            .build()
}
