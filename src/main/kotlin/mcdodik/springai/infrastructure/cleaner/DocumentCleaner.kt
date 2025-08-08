package mcdodik.springai.infrastructure.cleaner

import java.io.InputStream
import mcdodik.springai.api.dto.PdfCleanRequest
import org.springframework.stereotype.Component

@Component
interface DocumentCleaner {

    fun doIt(input: InputStream, params: PdfCleanRequest): InputStream

}