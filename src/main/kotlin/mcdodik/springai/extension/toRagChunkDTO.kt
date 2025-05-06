package mcdodik.springai.extension

import mcdodik.springai.model.RagChunkDTO
import org.springframework.ai.document.Document

fun Document.toRagChunkDTO(source: String?, index: Int, embedding: List<Float>): RagChunkDTO {
    return RagChunkDTO(
        content = this.text!!,
        embedding = embedding,
        source = source,
        chunkIndex = index,
        type = detectType(this.text!!),
        summary = null,
    )
}

private fun detectType(content: String): String {
    val trimmed = content.trim()

    // Простые эвристики
    if (trimmed.contains(Regex("""[;{}]""")) &&
        trimmed.contains(Regex("""\b(for|while|int|String|System\.out|public|class)\b"""))
    ) return "code"

    if (trimmed.lines().all { it.trim().startsWith("- ") || it.trim().matches(Regex("""\d+\.\s+.*""")) }) {
        return "list"
    }

    if (trimmed.lines().all { it.trim().startsWith(">") || it.trim().startsWith("\"") }) {
        return "quote"
    }

    if (trimmed.lines().count { it.contains("|") } >= 2) {
        return "table"
    }

    if (trimmed.length < 60 && trimmed == trimmed.uppercase()) {
        return "title"
    }

    if (trimmed.contains("![") || trimmed.contains("<img") || trimmed.contains("image:")) {
        return "image"
    }

    return "text"
}
