package com.alvaroquintana.usecases

import com.alvaroquintana.data.repository.RankingRepository
import dev.zacsweers.metro.Inject

@Inject
class GetRecordScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(limit: Long, gameMode: String = "Classic"): String =
        rankingRepository.getWorldRecords(limit, gameMode)

}
