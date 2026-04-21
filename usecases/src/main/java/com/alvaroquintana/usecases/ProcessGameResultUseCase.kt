package com.alvaroquintana.usecases

import com.alvaroquintana.data.datasource.GameResultProcessorDataSource
import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.ProcessedGameResult

class ProcessGameResultUseCase(
    private val gameResultProcessor: GameResultProcessorDataSource
) {
    suspend fun invoke(gameResult: GameResult): ProcessedGameResult {
        return gameResultProcessor.processGameResult(gameResult)
    }
}
