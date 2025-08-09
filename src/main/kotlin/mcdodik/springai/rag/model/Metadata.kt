package mcdodik.springai.rag.model

import mcdodik.springai.db.entity.rag.MetadataKey

object Metadata {
    fun embedding(d: RetrievedDoc): FloatArray? {
        val v = d.metadata[MetadataKey.EMBEDDING.key]
        return when (v) {
            is FloatArray -> v
            is List<*> -> v.filterIsInstance<Number>().map { it.toFloat() }.toFloatArray()
            else -> null
        }
    }

    fun fileName(d: RetrievedDoc): String? = d.metadata[MetadataKey.FILE_NAME.key] as? String

    fun chunkIndex(d: RetrievedDoc): Int? = (d.metadata[MetadataKey.CHUNK_INDEX.key] as? Number)?.toInt()

    fun fileName(d: org.springframework.ai.document.Document): String? = d.metadata[MetadataKey.FILE_NAME.key] as? String

    fun chunkIndex(d: org.springframework.ai.document.Document): Int? = (d.metadata[MetadataKey.CHUNK_INDEX.key] as? Number)?.toInt()

    fun embedding(d: org.springframework.ai.document.Document): FloatArray? {
        val k = MetadataKey.EMBEDDING.key
        val any = d.metadata[k]
        return when (any) {
            is FloatArray -> any
            is List<*> -> any.mapNotNull { (it as? Number)?.toFloat() }.toFloatArray()
            else -> null
        }
    }
}
