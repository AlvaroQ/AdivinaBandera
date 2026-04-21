package com.alvaroquintana.domain

data class GameResult(
    val gameMode: String,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val bestStreak: Int,
    val timePlayedMs: Long,
    val completedAllQuestions: Boolean
)
