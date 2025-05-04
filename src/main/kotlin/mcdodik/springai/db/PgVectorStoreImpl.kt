package mcdodik.springai.db

import mcdodik.springai.extension.toRagChunkDTO
import mcdodik.springai.model.RagChunkDTO
import mcdodik.springai.prerag.RagChunkDto
import mcdodik.springai.utils.embeded.Embedder
import org.springframework.ai.document.Document
import org.springframework.ai.vectorstore.SearchRequest
import org.springframework.ai.vectorstore.VectorStore
import org.springframework.ai.vectorstore.filter.Filter
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("CustomPgVectorStore")
class PgVectorStoreImpl(
    private val embedder: Embedder,
    private val ragChunkMapper: RagChunkMapper
) : VectorStore, CustomVectorStore {

    override fun write(documents: List<Document>) {
        val source = documents.firstOrNull()?.metadata?.get("source") as? String
        val chunks = documents.mapIndexed { index, doc ->
            val embedding = embedder.embed(doc.text!!)
            doc.toRagChunkDTO(source, index, embedding)
        }

        val entities = chunks.map {
            RagChunk(
                id = it.id,
                content = it.content,
                embedding = it.embedding,
                type = it.type,
                source = it.source,
                chunkIndex = it.chunkIndex
            )
        }
        for (entity in entities) {
            ragChunkMapper.insert(entity)
        }

    }

    override fun search(query: String): List<RagChunkDto> {
        val embedding = embedder.embed(query)
        return ragChunkMapper.searchByEmbedding(embedding)
            .map { RagChunkDto(it.content, it.type) }
    }

    override fun add(documents: List<Document?>) {
        TODO("Not yet implemented")
    }

    override fun delete(idList: List<String?>) {
        TODO("Not yet implemented")
    }

    override fun delete(filterExpression: Filter.Expression) {
        TODO("Not yet implemented")
    }

    override fun similaritySearch(request: SearchRequest): List<Document?>? {
        TODO("Not yet implemented")
    }

}
