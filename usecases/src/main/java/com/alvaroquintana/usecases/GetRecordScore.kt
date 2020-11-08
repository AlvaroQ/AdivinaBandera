package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.RankingRepository

class GetRecordScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(limit: Long): String = rankingRepository.getWorldRecords(limit)

}
