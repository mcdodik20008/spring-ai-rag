package mcdodik.springai.infrastructure.document.worker

import mcdodik.springai.api.dto.CleanRequestParams
import org.springframework.ai.document.Document
import org.springframework.web.multipart.MultipartFile

interface DocumentWorker {
    fun supports(file: MultipartFile): Boolean

    fun process(
        file: MultipartFile,
        params: CleanRequestParams,
    ): List<Document>
}
