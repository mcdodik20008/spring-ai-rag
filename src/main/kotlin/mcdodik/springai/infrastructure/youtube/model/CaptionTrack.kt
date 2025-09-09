package mcdodik.springai.infrastructure.youtube.model

data class CaptionTrack(
    val baseUrl: String,
    val name: Map<String, Any>?,
    val vssId: String?,
    val languageCode: String,
    val kind: String? = null,
    val isTranslatable: Boolean = false,
)
