package mcdodik.springai.config

import org.springframework.ai.transformer.splitter.TokenTextSplitter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AiConfig {

    @Bean
    fun tokenTextSplitter(): TokenTextSplitter = TokenTextSplitter(
        1000,     // chunkSize
        256,      // minChunkSizeChars
        128,      // minChunkLengthToEmbed
        1000,     // maxNumChunks
        true      // keepSeparator
    )

}
