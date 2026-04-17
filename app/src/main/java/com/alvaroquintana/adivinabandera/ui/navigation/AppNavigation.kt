package com.alvaroquintana.adivinabandera.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
data object Splash

@Serializable
data object Main

@Serializable
data object Select

@Serializable
data class Game(val mode: String = "Classic")

@Serializable
data class Practice(val countryIds: List<Int>)

@Serializable
data class Result(
    val points: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val bestStreak: Int,
    val timePlayedMs: Long,
    val completedAllQuestions: Boolean,
    val gameMode: String = "Classic"
)

@Serializable
data object Ranking

@Serializable
data object Info

@Serializable
data object Settings

@Serializable
data object Profile

@Serializable
data object XpLeaderboard

@Serializable
data object Shop
