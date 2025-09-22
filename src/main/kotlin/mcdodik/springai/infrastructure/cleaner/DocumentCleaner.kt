package mcdodik.springai.infrastructure.cleaner

import mcdodik.springai.api.dto.ingest.PdfCleanRequest
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
interface DocumentCleaner {
    fun doIt(
        input: InputStream,
        params: PdfCleanRequest,
    ): InputStream
}
