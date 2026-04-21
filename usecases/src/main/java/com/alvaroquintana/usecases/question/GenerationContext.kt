package com.alvaroquintana.usecases.question

data class GenerationContext(
    val excludedCountryIds: Set<Int> = emptySet(),
    val forcedCountryPool: List<Int> = emptyList(),
    val seenSubdivisionIds: Set<String> = emptySet(),
    val subdivisionAlpha2: String? = null
)
