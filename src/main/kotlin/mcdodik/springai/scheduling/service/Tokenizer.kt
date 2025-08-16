package mcdodik.springai.scheduling.service

interface Tokenizer {
    fun tokens(text: String): List<String>
}
