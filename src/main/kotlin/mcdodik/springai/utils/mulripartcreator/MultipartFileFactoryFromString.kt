package mcdodik.springai.utils.mulripartcreator

import java.nio.charset.Charset
import java.util.UUID
import mcdodik.springai.api.controller.responses.CustomMultipartFile
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class MultipartFileFactoryFromString : MultipartFileFactory<String> {

    override fun supports(input: Any): Boolean = input is String

    override fun create(input: String): MultipartFile {
        val filename = "file-${UUID.randomUUID()}.txt"

        return CustomMultipartFile(
            name = "file",
            originalFilename = filename,
            contentType = MediaType.TEXT_PLAIN_VALUE,
            content = input.toByteArray(Charset.forName("UTF-8"))
        )
    }
}