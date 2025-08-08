package mcdodik.springai.db.model.rag

enum class DocumentMetadataKey(val key: String) {
    EMBEDDING("embedding"),
    TYPE("type"),
    SOURCE("source"),
    CHUNK_INDEX("chunk_index"),
    FILE_NAME("file_name"),
    EXTENSION("extension"),
    HASH("hash");
}