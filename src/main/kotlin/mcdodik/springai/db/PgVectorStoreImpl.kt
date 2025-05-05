package mcdodik.springai.db

import mcdodik.springai.extension.toRagChunkDTO
import mcdodik.springai.prerag.RagChunkDto
import org.springframework.ai.chat.client.ChatClient
import org.springframework.ai.document.Document
import org.springframework.ai.ollama.OllamaEmbeddingModel
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.Filter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

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
            val summary = summarizer.prompt(it.content).call().content().toString()

            RagChunk(
                id = it.id,
                content = it.content,
                embedding = it.embedding,
                type = it.type,
                source = it.source,
                chunkIndex = it.chunkIndex,
                summary = summary
            )
        }
        for (entity in entities) {
            ragChunkMapper.insert(entity)
        }
    }

    override fun search(query: String): List<RagChunkDto> {
        val embedding = embeddingModel.embed(query).toList()
        return ragChunkMapper.searchByEmbedding(embedding).map { RagChunkDto(it.content, it.type) }
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

}
