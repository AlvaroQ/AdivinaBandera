package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.XpSyncKeys
import com.alvaroquintana.data.datasource.XpLeaderboardDataSource
import com.alvaroquintana.domain.XpLeaderboardEntry
import com.alvaroquintana.usecases.engagement.XpSyncService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class XpSyncManager(
    private val dataStore: DataStore<Preferences>,
    private val progressionManager: ProgressionManager,
    private val gameStatsManager: GameStatsManager,
    private val xpLeaderboardDataSource: XpLeaderboardDataSource
) : XpSyncService {

    private val mutex = Mutex()

    // Sincroniza el XP del usuario tras finalizar una partida
    // Si no tiene nickname configurado, marca la sincronizacion como pendiente
    override suspend fun syncAfterGame() = mutex.withLock {
        FirebaseCrashlytics.getInstance().log("xp_sync_after_game_started")
        val nickname = progressionManager.getNickname()
        if (nickname.isBlank()) {
            FirebaseCrashlytics.getInstance().log("xp_sync_pending_missing_nickname")
            markPendingSync()
            return@withLock
        }

        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            FirebaseCrashlytics.getInstance().log("xp_sync_pending_missing_uid")
            markPendingSync()
            return@withLock
        }

        try {
            val entry = buildFullEntry(uid)
            xpLeaderboardDataSource.syncUserEntry(entry)
            clearPendingSync()
            FirebaseCrashlytics.getInstance().log("xp_sync_after_game_success")
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().log("xp_sync_after_game_failed")
            FirebaseCrashlytics.getInstance().recordException(e)
            markPendingSync()
        }
    }

    // Reintenta la sincronizacion pendiente si existe
    suspend fun syncPendingIfNeeded() {
        val pending = dataStore.data.map { it[XpSyncKeys.PENDING_SYNC] ?: false }.first()
        if (pending) {
            FirebaseCrashlytics.getInstance().log("xp_sync_pending_detected")
            syncAfterGame()
        } else {
            FirebaseCrashlytics.getInstance().log("xp_sync_pending_not_required")
        }
    }

    // Construye la entrada completa combinando datos de progresion y estadisticas
    private suspend fun buildFullEntry(uid: String): XpLeaderboardEntry {
        val base = progressionManager.buildLeaderboardEntry(uid)
        return base.copy(
            totalGamesPlayed = gameStatsManager.getTotalGamesPlayed(),
            accuracy = gameStatsManager.getAccuracy()
        )
    }

    private suspend fun markPendingSync() {
        dataStore.edit { it[XpSyncKeys.PENDING_SYNC] = true }
    }

    private suspend fun clearPendingSync() {
        dataStore.edit {
            it[XpSyncKeys.PENDING_SYNC] = false
            it[XpSyncKeys.LAST_SYNC_TIMESTAMP] = System.currentTimeMillis()
        }
    }
}
