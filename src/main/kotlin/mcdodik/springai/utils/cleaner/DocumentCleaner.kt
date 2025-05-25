package mcdodik.springai.utils.cleaner

import mcdodik.springai.controller.model.PdfCleanRequest
import org.springframework.stereotype.Component
import java.io.InputStream

@Component
interface DocumentCleaner {

    fun doIt(input: InputStream, params: PdfCleanRequest): InputStream

}