package mcdodik.springai.scheduling.service

import org.springframework.stereotype.Component

@Component
class SimpleRuTokenizer : Tokenizer {
    // KISS: в проде добавь лемматизатор (mystem/pymorphy через сервис, или Lucene Morphology)
    private val regex = Regex("[\\p{L}\\p{Nd}]+", RegexOption.IGNORE_CASE)

    override fun tokens(text: String): List<String> = regex.findAll(text.lowercase()).map { it.value }.toList()
}
