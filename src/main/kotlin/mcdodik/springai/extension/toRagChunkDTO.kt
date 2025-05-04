package mcdodik.springai.extension

import mcdodik.springai.model.RagChunkDTO
import org.springframework.ai.document.Document

fun Document.toRagChunkDTO(source: String?, index: Int, embedding: List<Float>): RagChunkDTO {
    return RagChunkDTO(
        content = this.text!!,
        embedding = embedding,
        source = source,
        chunkIndex = index,
        type = isLikelyCodeBlock(this.text!!)
    )
}


private fun isLikelyCodeBlock(text: String): String {
    val keywords = listOf("if", "else", "while", "for", "return", "int", "System.out", "=", ";", "{", "}")
    val lines = text.lines()
    val keywordCount = lines.count { line ->
        keywords.any { kw -> line.contains(kw) }
    }
    val semicolonRatio = text.count { it == ';' }.toDouble() / text.length
    val braceRatio = text.count { it == '{' || it == '}' }.toDouble() / text.length

    if (keywordCount > 2 || semicolonRatio > 0.01 || braceRatio > 0.01) {
        return "code"
    }

    return "text"
}