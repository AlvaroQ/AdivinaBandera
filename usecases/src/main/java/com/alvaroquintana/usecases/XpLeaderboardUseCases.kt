package com.alvaroquintana.usecases

import com.alvaroquintana.data.datasource.XpLeaderboardDataSource
import com.alvaroquintana.domain.XpLeaderboardEntry

class SyncUserXpUseCase(private val dataSource: XpLeaderboardDataSource) {
    suspend fun invoke(entry: XpLeaderboardEntry) = dataSource.syncUserEntry(entry)
}

class GetXpLeaderboardUseCase(private val dataSource: XpLeaderboardDataSource) {
    suspend fun invoke(limit: Int = 100): List<XpLeaderboardEntry> = dataSource.getLeaderboard(limit)
}

class GetUserGlobalRankUseCase(private val dataSource: XpLeaderboardDataSource) {
    suspend fun invoke(uid: String): Int = dataSource.getUserRank(uid)
}
