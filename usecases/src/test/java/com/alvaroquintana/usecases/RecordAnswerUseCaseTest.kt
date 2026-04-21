package com.alvaroquintana.usecases

import com.alvaroquintana.domain.challenge.ChallengeCompletionResult
import com.alvaroquintana.domain.challenge.ChallengeEvent
import com.alvaroquintana.usecases.engagement.CountryMasteryService
import com.alvaroquintana.usecases.engagement.DailyChallengeService
import com.alvaroquintana.usecases.engagement.ProgressionService
import com.alvaroquintana.usecases.engagement.RegionalProgressionService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RecordAnswerUseCaseTest {

    private lateinit var countryMastery: CountryMasteryService
    private lateinit var regionalProgression: RegionalProgressionService
    private lateinit var progression: ProgressionService
    private lateinit var dailyChallenge: DailyChallengeService
    private lateinit var useCase: RecordAnswerUseCase

    @Before
    fun setUp() {
        countryMastery = mockk(relaxed = true)
        regionalProgression = mockk(relaxed = true)
        progression = mockk()
        dailyChallenge = mockk()
        coEvery { progression.getCurrentLevel() } returns 5
        coEvery { dailyChallenge.processEvent(any(), any()) } returns
            ChallengeCompletionResult(emptyList(), false, 0, 0)

        useCase = RecordAnswerUseCase(countryMastery, regionalProgression, progression, dailyChallenge)
    }

    @Test
    fun `correct answer records mastery and dispatches AnswerGiven correct`() = runTest {
        useCase.invoke(
            isCorrect = true,
            alpha2Code = "ES",
            gameMode = "Classic",
            regionalAlpha2 = null
        )

        coVerify { countryMastery.recordAnswer("ES", true, "Classic") }
        coVerify(exactly = 0) { regionalProgression.recordCorrectAnswer(any()) }
        coVerify {
            dailyChallenge.processEvent(
                event = match { it is ChallengeEvent.AnswerGiven && it.isCorrect },
                playerLevel = 5
            )
        }
    }

    @Test
    fun `wrong answer records mastery but does not advance regional chain`() = runTest {
        useCase.invoke(
            isCorrect = false,
            alpha2Code = "FR",
            gameMode = "Classic",
            regionalAlpha2 = "ES"
        )

        coVerify { countryMastery.recordAnswer("FR", false, "Classic") }
        coVerify(exactly = 0) { regionalProgression.recordCorrectAnswer(any()) }
        coVerify {
            dailyChallenge.processEvent(
                event = match { it is ChallengeEvent.AnswerGiven && !it.isCorrect },
                playerLevel = 5
            )
        }
    }

    @Test
    fun `regional correct answer advances the regional chain`() = runTest {
        useCase.invoke(
            isCorrect = true,
            alpha2Code = null,
            gameMode = "RegionSpain",
            regionalAlpha2 = "ES"
        )

        coVerify { regionalProgression.recordCorrectAnswer("ES") }
        coVerify(exactly = 0) { countryMastery.recordAnswer(any(), any(), any()) }
    }

    @Test
    fun `blank alpha2Code skips country mastery recording`() = runTest {
        useCase.invoke(
            isCorrect = true,
            alpha2Code = "",
            gameMode = "Classic",
            regionalAlpha2 = null
        )

        coVerify(exactly = 0) { countryMastery.recordAnswer(any(), any(), any()) }
    }
}
