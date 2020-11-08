package com.alvaroquintana.usecases

import arrow.core.Either
import com.alvaroquintana.data.repository.RankingRepository
import com.alvaroquintana.data.repository.RepositoryException
import com.alvaroquintana.domain.User

class SaveTopScore(private val rankingRepository: RankingRepository) {

    suspend fun invoke(user: User): Either<RepositoryException, User> = rankingRepository.addRecord(user)

}
