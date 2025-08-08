package mcdodik.springai.infrastructure.document.worker

import mcdodik.springai.api.dto.CleanRequestParams
import mcdodik.springai.config.chatmodel.ChatModelTemplates.EXTRACT_CHUNKS_PROMPT
import mcdodik.springai.extensions.featAllTextFromObsidianMd
import mcdodik.springai.extensions.fetchInfoFromFile
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

class LLMDocumentWorker(
    @Qualifier("openRouterChatClient")
    private val chunkExtractor: ChatClient,
) : DocumentWorker {



    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "text/markdown" || file.originalFilename?.endsWith(".md") == true

    override fun process(file: MultipartFile, params: CleanRequestParams): List<Document> {
        val text = file.featAllTextFromObsidianMd()
        val chunkedText = chunkExtractor
            .prompt(EXTRACT_CHUNKS_PROMPT)
            .user(text)
            .call()
            .content() ?: throw NullPointerException("summary is null")

        val chunksStr = parseChunks(chunkedText)
        val chunks = chunksStr.map { Document(it) }
        chunks.forEachIndexed { n, chunk -> chunk.fetchInfoFromFile(n, file) }
        return chunks
    }

    private fun parseChunks(rawText: String, maxChunks: Int = 20): List<String> {
        val normalized = rawText
            .replace("\r\n", "\n")
            .replace("\r", "\n")
            .replace("\\n", "\n") // если приходит как строка

        return normalized
            .split(Regex("""\n\s*\n""")) // <== разделяем по пустой строке (возможно с пробелами/табами)
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .take(maxChunks)
    }

}
