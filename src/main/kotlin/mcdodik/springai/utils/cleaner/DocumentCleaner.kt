package mcdodik.springai.utils.cleaner

import java.io.InputStream
import mcdodik.springai.api.controller.responses.PdfCleanRequest
import org.springframework.stereotype.Component

@Component
interface DocumentCleaner {

    fun doIt(input: InputStream, params: PdfCleanRequest): InputStream

}