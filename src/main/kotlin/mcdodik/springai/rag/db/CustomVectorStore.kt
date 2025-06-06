package mcdodik.springai.rag.db

import mcdodik.springai.rag.model.RagChunkDto
import org.springframework.ai.document.Document

interface CustomVectorStore {
    fun write(documents: List<Document>)
    fun search(query: String): List<RagChunkDto>
}
