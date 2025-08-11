package mcdodik.springai.apiai.v1.serivces

import mcdodik.springai.apiai.v1.dto.chat.ChatRequestDto
import mcdodik.springai.apiai.v1.dto.chat.ChatResponseDto
import org.springframework.http.codec.ServerSentEvent
import kotlinx.coroutines.flow.Flow

interface ChatService {
    suspend fun complete(req: ChatRequestDto): ChatResponseDto

    fun stream(req: ChatRequestDto): Flow<ServerSentEvent<Any>>
}
