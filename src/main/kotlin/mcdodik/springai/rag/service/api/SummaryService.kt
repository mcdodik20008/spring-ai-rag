package mcdodik.springai.rag.service.api

interface SummaryService {
    fun summariesByFileName(fileNames: Set<String>): Map<String, String>
}
