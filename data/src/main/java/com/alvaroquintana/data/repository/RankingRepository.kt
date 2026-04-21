package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.FirestoreDataSource
import com.alvaroquintana.domain.User

class RankingRepository(private val firestoreDataSource: FirestoreDataSource) {

    suspend fun addRecord(user: User, gameMode: String = "Classic") = firestoreDataSource.addRecord(user, gameMode)

    suspend fun getRanking(gameMode: String = "Classic"): MutableList<User> = firestoreDataSource.getRanking(gameMode)

    suspend fun getWorldRecords(limit: Long, gameMode: String = "Classic"): String = firestoreDataSource.getWorldRecords(limit, gameMode)
}
