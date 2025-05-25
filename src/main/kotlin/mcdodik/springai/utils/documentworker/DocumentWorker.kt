package mcdodik.springai.utils.documentworker

import mcdodik.springai.controller.model.PdfCleanRequest
import org.springframework.ai.document.Document
import org.springframework.web.multipart.MultipartFile

interface DocumentWorker {
    fun supports(file: MultipartFile): Boolean
    fun process(file: MultipartFile, params: PdfCleanRequest): List<Document>
}
