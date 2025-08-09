package mcdodik.springai.rag.model

import org.springframework.ai.document.Document

data class ScoredDoc(val doc: RetrievedDoc, val score: Double)