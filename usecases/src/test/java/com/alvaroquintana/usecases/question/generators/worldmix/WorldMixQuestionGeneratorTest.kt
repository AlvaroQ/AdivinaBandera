package com.alvaroquintana.usecases.question.generators.worldmix

import com.alvaroquintana.domain.Country
import com.alvaroquintana.domain.MixQuestionType
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.GetRandomCountries
import com.alvaroquintana.usecases.question.GenerationContext
import com.alvaroquintana.usecases.question.generators.ClassicQuestionGenerator
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class WorldMixQuestionGeneratorTest {

    private lateinit var getCountryById: GetCountryById
    private lateinit var getRandomCountries: GetRandomCountries
    private lateinit var composite: WorldMixQuestionGenerator

    @Before
    fun setUp() {
        getCountryById = mockk()
        getRandomCountries = mockk()
        val total = 10
        composite = WorldMixQuestionGenerator(
            flagToCountry = ClassicQuestionGenerator(getCountryById, total),
            flagToCapital = FlagToCapitalQuestionGenerator(getCountryById, total),
            currencyToCountry = CurrencyToCountryQuestionGenerator(getCountryById, total),
            demonymToCountry = DemonymQuestionGenerator(getCountryById, total),
            languageToCountry = LanguageQuestionGenerator(getCountryById, total),
            callingCodeToCountry = CallingCodeQuestionGenerator(getCountryById, total),
            neighborChallenge = NeighborChallengeQuestionGenerator(getCountryById, getRandomCountries, total)
        )
    }

    @Test
    fun `falls back to FLAG_TO_COUNTRY when other generators cannot produce`() = runTest {
        // Countries have no capital/currency/demonym/language/callingCode/borders → only FLAG_TO_COUNTRY works
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", icon = "🏳️$id", alpha2Code = "X$id")
        }
        coEvery { getRandomCountries.invoke(any()) } returns emptyList()

        val q = composite.generate(GenerationContext())

        assertNotNull(q)
        assertTrue(q!!.currentMixType == MixQuestionType.FLAG_TO_COUNTRY)
        assertTrue(q.flagIcon.startsWith("🏳️"))
    }

    @Test
    fun `sets currentMixType on the returned question`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", icon = "🏳️$id", alpha2Code = "X$id")
        }
        coEvery { getRandomCountries.invoke(any()) } returns emptyList()

        val q = composite.generate(GenerationContext())

        assertNotNull(q)
        assertNotNull(q!!.currentMixType)
    }
}
