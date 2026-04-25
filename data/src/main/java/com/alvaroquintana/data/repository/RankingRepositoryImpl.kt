package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.FirestoreDataSource
import com.alvaroquintana.domain.User
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class RankingRepositoryImpl(private val firestoreDataSource: FirestoreDataSource) : RankingRepository {

    override suspend fun addRecord(user: User, gameMode: String): Result<User> =
        firestoreDataSource.addRecord(user, gameMode)

    override suspend fun getRanking(gameMode: String): MutableList<User> =
        firestoreDataSource.getRanking(gameMode)

    override suspend fun getWorldRecords(limit: Long, gameMode: String): String =
        firestoreDataSource.getWorldRecords(limit, gameMode)
}
