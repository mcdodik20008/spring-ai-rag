package mcdodik.springai.infrastructure.multipart

import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class DelegatingMultipartFileFactory(
    private val factories: List<MultipartFileFactory<*>>,
) {
    @Suppress("UNCHECKED_CAST")
    fun create(input: Any): MultipartFile {
        val factory =
            factories
                .firstOrNull { it.supports(input) }
                ?: throw IllegalArgumentException("Неподдерживаемый тип: ${input::class}")
        return (factory as MultipartFileFactory<Any>).create(input)
    }
}
