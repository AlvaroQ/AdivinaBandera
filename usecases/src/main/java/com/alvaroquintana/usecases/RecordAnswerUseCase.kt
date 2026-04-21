package com.alvaroquintana.usecases

import com.alvaroquintana.domain.challenge.ChallengeEvent
import com.alvaroquintana.usecases.engagement.CountryMasteryService
import com.alvaroquintana.usecases.engagement.DailyChallengeService
import com.alvaroquintana.usecases.engagement.ProgressionService
import com.alvaroquintana.usecases.engagement.RegionalProgressionService

/**
 * Records a single in-game answer. Updates per-country mastery, advances the
 * regional chain when correct, and forwards the event to the daily challenge
 * engine. Previously this orchestration was duplicated inside GameViewModel.
 */
class RecordAnswerUseCase(
    private val countryMastery: CountryMasteryService,
    private val regionalProgression: RegionalProgressionService,
    private val progression: ProgressionService,
    private val dailyChallenge: DailyChallengeService
) {
    suspend fun invoke(
        isCorrect: Boolean,
        alpha2Code: String?,
        gameMode: String,
        regionalAlpha2: String?
    ) {
        alpha2Code?.takeIf { it.isNotBlank() }?.let {
            countryMastery.recordAnswer(it, isCorrect, gameMode)
        }
        if (isCorrect) {
            regionalAlpha2?.let { regionalProgression.recordCorrectAnswer(it) }
        }
        val playerLevel = progression.getCurrentLevel()
        dailyChallenge.processEvent(
            event = ChallengeEvent.AnswerGiven(isCorrect = isCorrect),
            playerLevel = playerLevel
        )
    }
}
