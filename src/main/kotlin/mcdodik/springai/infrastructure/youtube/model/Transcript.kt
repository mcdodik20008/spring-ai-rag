package mcdodik.springai.infrastructure.youtube.model

data class Transcript(
    val videoId: String,
    val language: String,
    val languageCode: String,
    val isGenerated: Boolean,
    val translationLanguages: List<String>,
) {
    suspend fun fetch(preserveFormatting: Boolean = false): List<TranscriptEntry> {
        // тут будет загрузка текста по baseUrl
        TODO("fetch implementation: $preserveFormatting")
    }

    suspend fun translate(lang: String): Transcript {
        // YouTube умеет автоперевод (через &tlang=), можно завернуть сюда
        TODO("translate implementation $lang")
    }
}
