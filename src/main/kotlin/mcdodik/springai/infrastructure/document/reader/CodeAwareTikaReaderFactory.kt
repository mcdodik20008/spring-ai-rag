package mcdodik.springai.infrastructure.document.reader

import org.springframework.ai.document.DocumentReader
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component

@Component
class CodeAwareTikaReaderFactory {
    fun create(resource: InputStreamResource): DocumentReader = CodeAwareTikaReader(resource)
}
