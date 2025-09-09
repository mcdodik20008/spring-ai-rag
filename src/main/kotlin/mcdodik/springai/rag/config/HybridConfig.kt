package mcdodik.springai.rag.config

import mcdodik.springai.rag.model.FuseMode

data class HybridConfig(
    val vecWeight: Double = 0.5,
    val bmWeight: Double = 0.5,
    val vecTopK: Int = 20,
    val bmTopK: Int = 20,
    val finalTopK: Int = 20,
    val mode: FuseMode = FuseMode.RRF,
    val rrfK: Int = 60,
)
