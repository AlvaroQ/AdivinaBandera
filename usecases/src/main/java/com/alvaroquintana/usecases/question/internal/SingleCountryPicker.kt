package com.alvaroquintana.usecases.question.internal

import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById

internal class SingleCountryPicker(
    private val getCountryById: GetCountryById,
    private val totalCountries: Int
) {
    /**
     * Picks an eligible country, honoring [forcedPool] (practice mode),
     * [excludedIds] (already-seen countries), and [filter] (mode-specific requirement).
     * Returns null if no eligible country is found within [maxAttempts].
     *
     * When [forcedPool] is non-empty the filter is ignored — practice mode respects the user's pool.
     */
    suspend fun pickEligible(
        excludedIds: Set<Int>,
        forcedPool: List<Int> = emptyList(),
        maxAttempts: Int = 200,
        filter: (Country) -> Boolean = { true }
    ): Pair<Int, Country>? {
        if (forcedPool.isNotEmpty()) {
            val available = forcedPool.filter { it !in excludedIds }
            val id = if (available.isNotEmpty()) available.random() else forcedPool.random()
            return id to getCountryById.invoke(id)
        }

        var attempts = 0
        while (attempts < maxAttempts) {
            val id = RandomUtils.randomWithExclusion(0, totalCountries, *excludedIds.toIntArray())
            val country = getCountryById.invoke(id)
            if (filter(country)) return id to country
            attempts++
        }
        return null
    }
}
