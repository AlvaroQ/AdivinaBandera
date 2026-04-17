package com.alvaroquintana.data.datasource

import com.alvaroquintana.domain.GameResult
import com.alvaroquintana.domain.ProcessedGameResult

interface GameResultProcessorDataSource {
    suspend fun processGameResult(gameResult: GameResult): ProcessedGameResult
}
