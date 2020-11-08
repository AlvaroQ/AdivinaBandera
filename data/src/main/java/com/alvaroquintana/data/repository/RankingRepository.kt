package com.alvaroquintana.data.repository

import com.alvaroquintana.data.datasource.FirestoreDataSource
import com.alvaroquintana.domain.User

class RankingRepository(private val firestoreDataSource: FirestoreDataSource) {

    suspend fun addRecord(user: User) = firestoreDataSource.addRecord(user)

    suspend fun getRanking(): MutableList<User> = firestoreDataSource.getRanking()

    suspend fun getWorldRecords(limit: Long): String = firestoreDataSource.getWorldRecords(limit)
}
