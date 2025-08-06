package mcdodik.springai.utils.documentworker

import mcdodik.springai.api.controller.model.CleanRequestParams
import org.springframework.ai.document.Document
import org.springframework.web.multipart.MultipartFile

interface DocumentWorker {
    fun supports(file: MultipartFile): Boolean
    fun process(file: MultipartFile, params: CleanRequestParams): List<Document>
}
