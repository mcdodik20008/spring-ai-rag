package mcdodik.springai.rag.formatting

import org.springframework.stereotype.Component

@Component
class ContextMarkdownFormatter {

    fun format(chunks: List<RagChunkDto>): String {
        return chunks.joinToString("\n\n---\n\n") { chunk ->
            when (chunk.type.lowercase()) {
                "code" -> """
                    ### [CODE SNIPPET]
                    ```kotlin
                    ${chunk.content.trim()}
                    ```
                """.trimIndent()

                "quote" -> """
                    ### [QUOTE]
                    > ${chunk.content.trim().replace("\n", "\n> ")}
                """.trimIndent()

                "list" -> """
                    ### [BULLET LIST]
                    ${chunk.content.lines().joinToString("\n") { "- ${it.trim()}" }}
                """.trimIndent()

                else -> """
                    ### [TEXT]
                    ${chunk.content.trim()}
                """.trimIndent()
            }
        }
    }
}
