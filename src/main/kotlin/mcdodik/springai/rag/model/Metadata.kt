package mcdodik.springai.rag.model

object Metadata {
    fun fileName(d: org.springframework.ai.document.Document): String? =
        d.metadata[mcdodik.springai.db.model.rag.DocumentMetadataKey.FILE_NAME.key] as? String
    fun chunkIndex(d: org.springframework.ai.document.Document): Int? =
        (d.metadata[mcdodik.springai.db.model.rag.DocumentMetadataKey.CHUNK_INDEX.key] as? Number)?.toInt()
    fun embedding(d: org.springframework.ai.document.Document): FloatArray? {
        val k = mcdodik.springai.db.model.rag.DocumentMetadataKey.EMBEDDING.key
        val any = d.metadata[k]
        return when (any) {
            is FloatArray -> any
            is List<*> -> any.mapNotNull { (it as? Number)?.toFloat() }.toFloatArray()
            else -> null
        }
    }
}