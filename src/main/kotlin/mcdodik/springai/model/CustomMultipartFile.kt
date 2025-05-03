package mcdodik.springai.model

import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream

class CustomMultipartFile(
    private val name: String,
    private val originalFilename: String,
    private val contentType: String?,
    private val content: ByteArray
) : MultipartFile {

    override fun getName(): String = name

    override fun getOriginalFilename(): String = originalFilename

    override fun getContentType(): String? = contentType

    override fun isEmpty(): Boolean = content.isEmpty()

    override fun getSize(): Long = content.size.toLong()

    override fun getBytes(): ByteArray = content

    override fun getInputStream(): InputStream = ByteArrayInputStream(content)

    override fun transferTo(dest: File) {
        dest.outputStream().use { it.write(content) }
    }
}