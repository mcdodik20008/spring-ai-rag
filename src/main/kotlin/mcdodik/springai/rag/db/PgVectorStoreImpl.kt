package mcdodik.springai.rag.db

import mcdodik.springai.config.Loggable
import mcdodik.springai.extension.toRagChunkDTO
import mcdodik.springai.rag.formatting.RagChunkDto
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.Filter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
@Qualifier("CustomPgVectorStore")
class PgVectorStoreImpl(
    private val embeddingModel: OllamaEmbeddingModel,
    private val ragChunkMapper: RagChunkMapper,
    @Qualifier("openRouterChatClient") private val summarizer: ChatClient,
) : VectorStore, CustomVectorStore {

    override fun write(documents: List<Document>) {
        val source = documents.firstOrNull()?.metadata?.get("source") as? String
        val chunks = documents.mapIndexed { index, doc ->
            val embedding = embeddingModel.embed(doc.text!!).toList()
            doc.toRagChunkDTO(source, index, embedding)
        }

        val entities = chunks.map {
            //val summary = summarizer.prompt(it.content).call().content().toString()

            RagChunk(
                id = it.id,
                content = it.content,
                embedding = it.embedding,
                type = it.type,
                source = it.source,
                chunkIndex = it.chunkIndex,
                createdAt = LocalDateTime.now(),
                summary = it.summary,
            )
        }
        for (entity in entities) {
            ragChunkMapper.insert(entity)
        }
    }

    override fun search(query: String): List<RagChunkDto> {
        val embedding = embeddingModel.embed(query).toList()
        logger.debug("Searching for {}", embedding)
        val result = ragChunkMapper.searchByEmbedding(embedding)
        logger.debug("Found chunk with ids^ {}", result.map { x -> x.id })
        return result.map { RagChunkDto(it.content, it.type) }
    }

    override fun add(documents: List<Document?>) {
        print("Спасибо, работаем братья!")
    }

    override fun delete(idList: List<String?>) {
        print("Спасибо, работаем братья!")
    }

    override fun delete(filterExpression: Filter.Expression) {
        print("Спасибо, работаем братья!")
    }

    override fun similaritySearch(request: SearchRequest): List<Document?>? {
        return search(request.query).map { x -> Document(x.content) }
    }

    companion object : Loggable
}
