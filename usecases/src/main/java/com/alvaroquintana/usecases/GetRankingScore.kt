package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.RankingRepository
import com.alvaroquintana.domain.User

class GetRankingScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(): MutableList<User> = rankingRepository.getRanking()

}
