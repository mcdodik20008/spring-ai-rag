package mcdodik.springai.rag.service.api

import mcdodik.springai.api.dto.CleanRequestParams
import org.springframework.web.multipart.MultipartFile
import reactor.core.publisher.Flux
import kotlinx.coroutines.flow.Flow

interface RagService {
    fun ask(question: String): Flux<String>

    fun askFlow(question: String): Flow<String>

    fun ingest(
        file: MultipartFile,
        params: CleanRequestParams,
    )
}
