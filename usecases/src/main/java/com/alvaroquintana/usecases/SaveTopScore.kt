package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.RankingRepository
import com.alvaroquintana.domain.User

class SaveTopScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(user: User, gameMode: String = "Classic"): Result<User> =
        rankingRepository.addRecord(user, gameMode)

}
