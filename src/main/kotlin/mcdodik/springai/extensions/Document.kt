package mcdodik.springai.extensions

import mcdodik.springai.db.entity.rag.MetadataKey
import mcdodik.springai.rag.model.RetrievedDoc
import mcdodik.springai.rag.model.ScoreType
import org.springframework.ai.document.Document
import org.springframework.web.multipart.MultipartFile

fun Document.fetchInfoFromFile(
    n: Int,
    file: MultipartFile,
) {
    this.metadata[MetadataKey.CHUNK_INDEX.key] = n
    this.metadata[MetadataKey.FILE_NAME.key] = file.originalFilename
    this.metadata[MetadataKey.EXTENSION.key] = file.contentType
    this.metadata[MetadataKey.HASH.key] = file.sha256()
}

fun Document.toRetrievedDoc(defaultType: ScoreType = ScoreType.VECTOR): RetrievedDoc {
    return RetrievedDoc(
        id = this.metadata[MetadataKey.ID.key].toString(),
        content = this.text ?: "",
        metadata = metadata,
        score = 0.0,
        type = defaultType,
    )
}
