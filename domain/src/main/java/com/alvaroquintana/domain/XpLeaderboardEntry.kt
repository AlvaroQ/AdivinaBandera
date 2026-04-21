package com.alvaroquintana.domain

data class XpLeaderboardEntry(
    val uid: String = "",
    val nickname: String = "",
    val imageBase64: String = "",
    val totalXp: Int = 0,
    val level: Int = 1,
    val title: String = "",
    val totalGamesPlayed: Int = 0,
    val accuracy: Float = 0f,
    val lastUpdated: Long = 0L,
    val createdAt: Long = 0L
)
