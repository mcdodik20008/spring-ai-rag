package mcdodik.springai.utils.documentworker

import mcdodik.springai.api.controller.model.CleanRequestParams
import mcdodik.springai.extension.featAllTextFromObsidianMd
import mcdodik.springai.extension.fetchInfoFromFile
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.web.multipart.MultipartFile

class LLMDocumentWorker(
    private val chunkExtractor: ChatClient,
) : DocumentWorker {

    val EXTRACT_CHUNKS_PROMPT = """
    Ты интеллектуальный редактор, готовящий длинный текст для поиска по векторной базе. 
    Твоя задача: 
        1. Выдели осмысленные смысловые блоки (темы, аргументы, тезисы, выводы). 
        2. Каждый блок должен быть самодостаточным — с ясной мыслью. 
        3. Каждый блок начинай с новой строки, а все остальное пиши в одну строку. 
        4. Информация должна быть сгруппирована так, что бы максимизировать коэффициент при поиске RAG 
        5. Не стоит выделять больше 15-20 блоков 
        6. Отправь только смысловые блоки без другого текста и нумерации блоков 
        7. Блок должен быть 300-800 токенов
        8. Используй релевантные термины из исходного текста — философские понятия, имена, категории — и сохраняй их контекстно связанной группой. Это увеличит точность RAG-поиска.
        9. Убирай вводные и дублирующие фразы. Каждый блок должен содержать только значимую и аналитическую информацию, без риторики.
        11. Один блок должен быть логически целостным: если в нём анализируется один философ или концепция — не смешивай его с другими без необходимости.
""".trimIndent()

    override fun supports(file: MultipartFile): Boolean =
        file.contentType == "text/markdown" || file.originalFilename?.endsWith(".md") == true

    override fun process(file: MultipartFile, params: CleanRequestParams): List<Document> {
        val text = file.featAllTextFromObsidianMd()
        val summary = chunkExtractor
            .prompt(EXTRACT_CHUNKS_PROMPT)
            .user(text)
            .call()
            .content() ?: throw NullPointerException("summary is null")

        val chunksStr = parseChunks(summary)
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
