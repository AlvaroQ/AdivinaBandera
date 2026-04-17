package com.alvaroquintana.data.datasource

import com.alvaroquintana.domain.XpLeaderboardEntry

interface XpLeaderboardDataSource {
    suspend fun syncUserEntry(entry: XpLeaderboardEntry)
    suspend fun getLeaderboard(limit: Int = 100): List<XpLeaderboardEntry>
    suspend fun getUserRank(uid: String): Int
}
