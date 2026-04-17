package com.alvaroquintana.adivinabandera.ui.game

import app.cash.turbine.test
import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.CountryMasteryManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryById
import com.alvaroquintana.usecases.GetRandomCountries
import com.alvaroquintana.usecases.GetSubdivisionsForCountry
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GameViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getCountryById: GetCountryById
    private lateinit var viewModel: GameViewModel

    // Pre-defined countries for deterministic test responses
    private val correctCountry = Country(name = "Argentina", alpha2Code = "AR", icon = "\uD83C\uDDE6\uD83C\uDDF7")
    private val wrongCountry1 = Country(name = "Brasil", alpha2Code = "BR", icon = "\uD83C\uDDE7\uD83C\uDDF7")
    private val wrongCountry2 = Country(name = "Chile", alpha2Code = "CL", icon = "\uD83C\uDDE8\uD83C\uDDF1")
    private val wrongCountry3 = Country(name = "Uruguay", alpha2Code = "UY", icon = "\uD83C\uDDFA\uD83C\uDDFE")

    @Before
    fun setUp() {
        mockkObject(Analytics)
        every { Analytics.analyticsScreenViewed(any()) } just runs
        every { Analytics.analyticsGameFinished(any()) } just runs

        getCountryById = mockk()
        // generateNewStage calls getCountryById 4 times with different IDs.
        // Use answerAll to return different countries for each sequential call.
        coEvery { getCountryById.invoke(any()) } returnsMany listOf(
            correctCountry,
            wrongCountry1,
            wrongCountry2,
            wrongCountry3
        )

        viewModel = GameViewModel(
            getCountryById = getCountryById,
            getRandomCountries = mockk(relaxed = true),
            getSubdivisionsForCountry = mockk(relaxed = true),
            dailyChallengeManager = mockk(relaxed = true),
            progressionManager = mockk(relaxed = true),
            countryMasteryManager = mockk(relaxed = true),
            regionalProgressionManager = mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `generateNewStage emits question and 4 response options`() = runTest {
        // Re-mock for a fresh generateNewStage call (init already consumed the first set)
        coEvery { getCountryById.invoke(any()) } returnsMany listOf(
            correctCountry,
            wrongCountry1,
            wrongCountry2,
            wrongCountry3
        )

        viewModel.responseOptions.test {
            viewModel.generateNewStage()
            val options = awaitItem()
            assertEquals(4, options.size)
            assertTrue("Correct country alpha2Code must be among the options",
                options.contains(correctCountry.alpha2Code))
        }
    }

    @Test
    fun `generateNewStage sets question to country icon`() = runTest {
        coEvery { getCountryById.invoke(any()) } returnsMany listOf(
            correctCountry,
            wrongCountry1,
            wrongCountry2,
            wrongCountry3
        )

        viewModel.generateNewStage()

        assertEquals(correctCountry.icon, viewModel.question.value)
    }

    @Test
    fun `navigateToResult emits Navigation Result`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToResult(points = "10", totalQuestions = 5, completedAllQuestions = false)
            val result = awaitItem()
            assertTrue(result is GameViewModel.Navigation.Result)
            assertEquals("10", (result as GameViewModel.Navigation.Result).points)
        }
    }

    @Test
    fun `showRewardedAd emits ShowRewardedAd`() = runTest {
        viewModel.showingAds.test {
            viewModel.showRewardedAd()
            val uiModel = awaitItem()
            assertTrue(uiModel is GameViewModel.UiModel.ShowRewardedAd)
            assertTrue((uiModel as GameViewModel.UiModel.ShowRewardedAd).show)
        }
    }

    @Test
    fun `getCode2CountryCorrect returns alpha2Code after generateNewStage`() = runTest {
        coEvery { getCountryById.invoke(any()) } returnsMany listOf(
            correctCountry,
            wrongCountry1,
            wrongCountry2,
            wrongCountry3
        )

        viewModel.generateNewStage()

        val code = viewModel.getCode2CountryCorrect()
        assertNotNull(code)
        assertEquals(correctCountry.alpha2Code, code)
    }
}
