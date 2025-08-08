package mcdodik.springai.rag.api

interface SummaryService {
    fun summariesByFileName(fileNames: Set<String>): Map<String, String>
}