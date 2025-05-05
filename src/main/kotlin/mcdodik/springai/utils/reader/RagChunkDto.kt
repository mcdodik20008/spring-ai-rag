package mcdodik.springai.utils.reader

data class RagChunkDto(val content: String, val type: String)

fun splitByCodeBlocks(content: String): List<RagChunkDto> {
    val result = mutableListOf<RagChunkDto>()
    val buffer = StringBuilder()
    var insideCode = false

    for (line in content.lines()) {
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (insideCode) {
                buffer.appendLine(line)
                result += RagChunkDto(buffer.toString().trim(), "code")
                buffer.clear()
                insideCode = false
            } else {
                if (buffer.isNotBlank()) {
                    result += RagChunkDto(buffer.toString().trim(), "text")
                    buffer.clear()
                }
                buffer.appendLine(line)
                insideCode = true
            }
        } else {
            buffer.appendLine(line)
        }
    }

    if (buffer.isNotBlank()) {
        val type = if (insideCode) "code" else "text"
        result += RagChunkDto(buffer.toString().trim(), type)
    }

    return result
}
