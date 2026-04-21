package com.alvaroquintana.usecases.question.generators

import com.alvaroquintana.domain.CountrySubdivision
import com.alvaroquintana.usecases.GetSubdivisionsForCountry
import com.alvaroquintana.usecases.question.GenerationContext
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SubdivisionQuestionGeneratorTest {

    private lateinit var getSubdivisionsForCountry: GetSubdivisionsForCountry
    private lateinit var generator: SubdivisionQuestionGenerator

    @Before
    fun setUp() {
        getSubdivisionsForCountry = mockk()
        generator = SubdivisionQuestionGenerator(getSubdivisionsForCountry)
    }

    private fun fakePool(countryAlpha2: String, size: Int): List<CountrySubdivision> =
        (1..size).map {
            CountrySubdivision(
                id = "$countryAlpha2-$it",
                countryAlpha2 = countryAlpha2,
                name = "Region$it",
                type = "t",
                flagUrl = "url$it",
                difficulty = "easy"
            )
        }

    @Test
    fun `returns null when no alpha2 in context`() = runTest {
        val q = generator.generate(GenerationContext(subdivisionAlpha2 = null))
        assertNull(q)
    }

    @Test
    fun `returns null when pool has fewer than 4 subdivisions`() = runTest {
        coEvery { getSubdivisionsForCountry.invoke("ES") } returns fakePool("ES", 3)
        val q = generator.generate(GenerationContext(subdivisionAlpha2 = "ES"))
        assertNull(q)
    }

    @Test
    fun `produces 4 options with correct among them and exposes subdivision id`() = runTest {
        coEvery { getSubdivisionsForCountry.invoke("ES") } returns fakePool("ES", 6)

        val q = generator.generate(GenerationContext(subdivisionAlpha2 = "ES"))

        assertNotNull(q)
        assertEquals(4, q!!.options.size)
        assertTrue(q.options.contains(q.correctAnswer))
        assertNotNull(q.seenSubdivisionId)
        assertTrue(q.flagIcon.startsWith("url"))
    }

    @Test
    fun `prefers subdivisions not yet seen`() = runTest {
        val pool = fakePool("ES", 5)
        coEvery { getSubdivisionsForCountry.invoke("ES") } returns pool
        val seenIds = pool.take(4).map { it.id }.toSet()

        val q = generator.generate(
            GenerationContext(subdivisionAlpha2 = "ES", seenSubdivisionIds = seenIds)
        )

        assertNotNull(q)
        // Only id not seen is the 5th — must be the correct one
        assertEquals(pool.last().id, q!!.seenSubdivisionId)
    }
}
