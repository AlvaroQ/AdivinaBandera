package com.alvaroquintana.usecases.question.internal

import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById

internal data class ThreeOptions(
    val o1: Country, val o2: Country, val o3: Country,
    val p1: Int, val p2: Int, val p3: Int
)

internal class WrongOptionsPicker(
    private val getCountryById: GetCountryById,
    private val totalCountries: Int
) {
    /**
     * Picks three distractor countries that satisfy [predicate] and assigns each a
     * random UI position distinct from [correctPosition].
     * Falls back to any country if the predicate cannot be met within 50 attempts per slot.
     */
    suspend fun pick(
        correctId: Int,
        correctPosition: Int,
        predicate: (Country) -> Boolean = { true }
    ): ThreeOptions {
        val maxAttempts = 50

        var id1: Int
        var c1: Country
        var att = 0
        do {
            id1 = RandomUtils.randomWithExclusion(1, totalCountries, correctId)
            c1 = getCountryById.invoke(id1)
            att++
        } while (!predicate(c1) && att < maxAttempts)
        val p1 = RandomUtils.randomWithExclusion(0, 3, correctPosition)

        var id2: Int
        var c2: Country
        att = 0
        do {
            id2 = RandomUtils.randomWithExclusion(1, totalCountries, correctId, id1)
            c2 = getCountryById.invoke(id2)
            att++
        } while (!predicate(c2) && att < maxAttempts)
        val p2 = RandomUtils.randomWithExclusion(0, 3, correctPosition, p1)

        var id3: Int
        var c3: Country
        att = 0
        do {
            id3 = RandomUtils.randomWithExclusion(1, totalCountries, correctId, id1, id2)
            c3 = getCountryById.invoke(id3)
            att++
        } while (!predicate(c3) && att < maxAttempts)
        val p3 = RandomUtils.randomWithExclusion(0, 3, correctPosition, p1, p2)

        return ThreeOptions(c1, c2, c3, p1, p2, p3)
    }
}
