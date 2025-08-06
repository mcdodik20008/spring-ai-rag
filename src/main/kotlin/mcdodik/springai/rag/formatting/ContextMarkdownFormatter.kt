package mcdodik.springai.rag.formatting

import org.springframework.ai.document.Document
import org.springframework.stereotype.Component

@Component
class ContextMarkdownFormatter {

    fun format(chunks: List<Document?>?): String {
        return chunks?.joinToString("\n\n---\n\n") { chunk ->
            when (chunk?.metadata?.get("BLOCK_TYPE")) {
                "code" -> """
                        ### [CODE SNIPPET]
                        ```kotlin
                        ${chunk.text?.trim()}
                        ```
                    """.trimIndent()

                "quote" -> """
                        ### [QUOTE]
                        >  ${chunk.text?.trim()?.replace("\n", "\n> ")}
                    """.trimIndent()

                "list" -> """
                        ### [BULLET LIST]
                        $ ${chunk.text?.lines()?.joinToString("\n") { "- ${it.trim()}" }}
                    """.trimIndent()

                else -> """
                        ### [TEXT]
                         ${chunk?.text?.trim()}
                    """.trimIndent()
            }
        }.toString()
    }
}
