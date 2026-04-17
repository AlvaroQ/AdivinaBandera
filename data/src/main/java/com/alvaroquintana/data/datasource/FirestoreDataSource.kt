package com.alvaroquintana.data.datasource

import com.alvaroquintana.domain.User

interface FirestoreDataSource {
    suspend fun addRecord(user: User, gameMode: String = "Classic"): Result<User>
    suspend fun getRanking(gameMode: String = "Classic"): MutableList<User>
    suspend fun getWorldRecords(limit: Long, gameMode: String = "Classic"): String
}
