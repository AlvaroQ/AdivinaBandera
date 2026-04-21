package com.alvaroquintana.adivinabandera.ui.game

import app.cash.turbine.test
import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.usecases.RecordAnswerUseCase
import com.alvaroquintana.usecases.question.GeneratedQuestion
import com.alvaroquintana.usecases.question.QuestionGenerator
import com.alvaroquintana.usecases.question.QuestionGeneratorFactory
import io.mockk.coEvery
import io.mockk.coVerify
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

    private lateinit var factory: QuestionGeneratorFactory
    private lateinit var recordAnswer: RecordAnswerUseCase
    private lateinit var generator: QuestionGenerator
    private lateinit var viewModel: GameViewModel

    private val classicQuestion = GeneratedQuestion(
        options = listOf("AR", "BR", "CL", "UY"),
        correctAnswer = "AR",
        flagIcon = "🇦🇷",
        selectedCountryId = 1,
        selectedCountryAlpha2 = "AR"
    )

    @Before
    fun setUp() {
        mockkObject(Analytics)
        every { Analytics.analyticsScreenViewed(any()) } just runs
        every { Analytics.analyticsGameFinished(any()) } just runs

        factory = mockk()
        recordAnswer = mockk(relaxed = true)
        generator = mockk()
        every { factory.forMode(any()) } returns generator
        coEvery { generator.generate(any()) } returns classicQuestion

        viewModel = GameViewModel(factory, recordAnswer)
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `generateNewStage emits question and 4 response options with correct answer present`() = runTest {
        viewModel.responseOptions.test {
            viewModel.generateNewStage()
            val options = awaitItem()
            assertEquals(4, options.size)
            assertTrue("Correct country alpha2Code must be among the options", options.contains("AR"))
        }
    }

    @Test
    fun `generateNewStage sets question to country icon`() = runTest {
        viewModel.generateNewStage()
        assertEquals(classicQuestion.flagIcon, viewModel.question.value)
    }

    @Test
    fun `getCode2CountryCorrect returns alpha2Code after generateNewStage`() = runTest {
        viewModel.generateNewStage()

        val code = viewModel.getCode2CountryCorrect()
        assertNotNull(code)
        assertEquals("AR", code)
    }

    @Test
    fun `getCorrectAnswer returns the correctAnswer from generated question`() = runTest {
        viewModel.generateNewStage()
        assertEquals("AR", viewModel.getCorrectAnswer())
    }

    @Test
    fun `onCorrectAnswer delegates to RecordAnswerUseCase with correct flag`() = runTest {
        viewModel.generateNewStage()
        viewModel.onCorrectAnswer()

        coVerify {
            recordAnswer.invoke(
                isCorrect = true,
                alpha2Code = "AR",
                gameMode = "Classic",
                regionalAlpha2 = null
            )
        }
    }

    @Test
    fun `onWrongAnswer resets streak and delegates to RecordAnswerUseCase`() = runTest {
        viewModel.generateNewStage()
        viewModel.onCorrectAnswer() // streak = 1
        viewModel.onWrongAnswer()

        coVerify {
            recordAnswer.invoke(
                isCorrect = false,
                alpha2Code = "AR",
                gameMode = "Classic",
                regionalAlpha2 = null
            )
        }
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
    fun `regional mode forwards regionalAlpha2 to RecordAnswerUseCase`() = runTest {
        viewModel = GameViewModel(factory, recordAnswer, GameMode.RegionSpain)
        viewModel.generateNewStage()
        viewModel.onCorrectAnswer()

        coVerify {
            recordAnswer.invoke(
                isCorrect = true,
                alpha2Code = "AR",
                gameMode = "RegionSpain",
                regionalAlpha2 = "ES"
            )
        }
    }
}
