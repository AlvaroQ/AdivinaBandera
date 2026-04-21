package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.question.GenerationContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ClassicQuestionGeneratorTest {

    private lateinit var getCountryById: GetCountryById
    private lateinit var generator: ClassicQuestionGenerator

    @Before
    fun setUp() {
        getCountryById = mockk()
        generator = ClassicQuestionGenerator(getCountryById, totalCountries = 10)
    }

    @Test
    fun `produces 4 options with alpha2code as correct answer`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", icon = "🏳️$id", alpha2Code = "X$id")
        }

        val q = generator.generate(GenerationContext())

        assertNotNull(q)
        assertEquals(4, q!!.options.size)
        assertTrue(q.options.contains(q.correctAnswer))
        assertTrue(q.correctAnswer.startsWith("X"))
        assertTrue(q.flagIcon.startsWith("🏳️"))
        assertNotNull(q.selectedCountryId)
    }

    @Test
    fun `forcedCountryPool picks from pool when not in excluded`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", icon = "🏳️$id", alpha2Code = "X$id")
        }

        val context = GenerationContext(
            excludedCountryIds = setOf(5, 6),
            forcedCountryPool = listOf(5, 6, 7)
        )
        val q = generator.generate(context)

        assertNotNull(q)
        // Must pick id=7 — the only one not in excluded
        assertEquals(7, q!!.selectedCountryId)
    }

    @Test
    fun `options do not contain empty strings`() = runTest {
        coEvery { getCountryById.invoke(any()) } answers {
            val id = firstArg<Int>()
            Country(name = "C$id", alpha2Code = "X$id")
        }

        val q = generator.generate(GenerationContext())

        assertNotNull(q)
        assertFalse(q!!.options.any { it.isBlank() })
    }
}
