package mcdodik.springai.infrastructure.youtube.model

class TranscriptList(
    private val transcripts: List<Transcript>
) : Iterable<Transcript> {
    override fun iterator(): Iterator<Transcript> = transcripts.iterator()

    fun findTranscript(languages: List<String>): Transcript =
        transcripts.firstOrNull { it.languageCode in languages }
            ?: throw IllegalArgumentException("No transcript for languages $languages")

    fun findGeneratedTranscript(languages: List<String>): Transcript =
        transcripts.firstOrNull { it.languageCode in languages && it.isGenerated }
            ?: throw IllegalArgumentException("No generated transcript for $languages")

    fun findManuallyCreatedTranscript(languages: List<String>): Transcript =
        transcripts.firstOrNull { it.languageCode in languages && !it.isGenerated }
            ?: throw IllegalArgumentException("No manual transcript for $languages")
}
