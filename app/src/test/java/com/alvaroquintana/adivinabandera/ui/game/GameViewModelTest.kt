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

        viewModel = GameViewModel(
            gameMode = GameMode.Classic,
            forcedCountryPool = emptyList(),
            questionGeneratorFactory = factory,
            recordAnswer = recordAnswer
        )
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `GenerateNewStage updates state with 4 response options containing the correct answer`() = runTest {
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)
        val options = viewModel.state.value.responseOptions
        assertEquals(4, options.size)
        assertTrue("Correct country alpha2Code must be among the options", options.contains("AR"))
    }

    @Test
    fun `GenerateNewStage sets flagIcon to country icon`() = runTest {
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)
        assertEquals(classicQuestion.flagIcon, viewModel.state.value.flagIcon)
    }

    @Test
    fun `getCode2CountryCorrect returns alpha2Code after GenerateNewStage`() = runTest {
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)

        val code = viewModel.getCode2CountryCorrect()
        assertNotNull(code)
        assertEquals("AR", code)
    }

    @Test
    fun `getCorrectAnswer returns the correctAnswer from generated question`() = runTest {
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)
        assertEquals("AR", viewModel.getCorrectAnswer())
    }

    @Test
    fun `OnCorrectAnswer delegates to RecordAnswerUseCase with correct flag`() = runTest {
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)
        viewModel.dispatch(GameViewModel.Intent.OnCorrectAnswer)

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
    fun `OnWrongAnswer resets streak and delegates to RecordAnswerUseCase`() = runTest {
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)
        viewModel.dispatch(GameViewModel.Intent.OnCorrectAnswer) // streak = 1
        viewModel.dispatch(GameViewModel.Intent.OnWrongAnswer)

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
    fun `NavigateToResult intent emits Event NavigateToResult`() = runTest {
        viewModel.events.test {
            viewModel.dispatch(
                GameViewModel.Intent.NavigateToResult(
                    points = "10",
                    totalQuestions = 5,
                    completedAllQuestions = false
                )
            )
            val event = awaitItem()
            assertTrue(event is GameViewModel.Event.NavigateToResult)
            assertEquals("10", (event as GameViewModel.Event.NavigateToResult).points)
        }
    }

    @Test
    fun `ShowRewardedAd intent emits Event ShowRewardedAd`() = runTest {
        viewModel.events.test {
            viewModel.dispatch(GameViewModel.Intent.ShowRewardedAd)
            val event = awaitItem()
            assertEquals(GameViewModel.Event.ShowRewardedAd, event)
        }
    }

    @Test
    fun `regional mode forwards regionalAlpha2 to RecordAnswerUseCase`() = runTest {
        viewModel = GameViewModel(
            gameMode = GameMode.RegionSpain,
            forcedCountryPool = emptyList(),
            questionGeneratorFactory = factory,
            recordAnswer = recordAnswer
        )
        viewModel.dispatch(GameViewModel.Intent.GenerateNewStage)
        viewModel.dispatch(GameViewModel.Intent.OnCorrectAnswer)

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
