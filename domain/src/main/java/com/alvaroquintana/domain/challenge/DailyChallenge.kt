package com.alvaroquintana.domain.challenge

data class DailyChallenge(
    val id: String,           // "{date}_{difficulty}_{type}"
    val type: ChallengeType,
    val difficulty: ChallengeDifficulty,
    val description: String,
    val targetValue: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val reward: ChallengeReward,
    val extraParam: String = ""
)
