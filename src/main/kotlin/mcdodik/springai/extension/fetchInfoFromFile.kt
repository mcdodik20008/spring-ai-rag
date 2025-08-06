package mcdodik.springai.extension

import java.security.MessageDigest
import mcdodik.springai.db.model.DocumentMetadataKey
import org.springframework.ai.document.Document
import org.springframework.web.multipart.MultipartFile

fun Document.fetchInfoFromFile(n: Int, file: MultipartFile) {
    this.metadata[DocumentMetadataKey.CHUNK_INDEX.key] = n
    this.metadata[DocumentMetadataKey.FILE_NAME.key] = file.originalFilename
    this.metadata[DocumentMetadataKey.EXTENSION.key] = file.contentType
    this.metadata[DocumentMetadataKey.HASH.key] = file.sha256()
}

fun MultipartFile.sha256(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    inputStream.use {
        val buffer = ByteArray(8192)
        var bytesRead: Int
        while (it.read(buffer).also { bytesRead = it } != -1) {
            digest.update(buffer, 0, bytesRead)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

fun MultipartFile.featAllTextFromObsidianMd(): String {
    val text = this.inputStream.bufferedReader().use { it.readText() }

    val regex = Regex("^---\\s*\\n(.*?)\\n---\\s*\\n", RegexOption.DOT_MATCHES_ALL)
    return regex.replaceFirst(text, "").trim()
}