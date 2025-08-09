package mcdodik.springai.extensions

import java.security.MessageDigest
import org.springframework.web.multipart.MultipartFile

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