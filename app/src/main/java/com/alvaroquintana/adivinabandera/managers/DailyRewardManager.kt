package com.alvaroquintana.adivinabandera.managers

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.alvaroquintana.adivinabandera.common.DataStoreKeys.DailyRewardKeys
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class DailyRewardManager(private val dataStore: DataStore<Preferences>) {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    enum class RewardTier { COMMON, UNCOMMON, RARE }

    data class DailyReward(
        val tier: RewardTier,
        val xpAmount: Int,
        val coinsAmount: Int,
        val isClaimed: Boolean
    )

    private fun todayDate(): String = dateFormat.format(Calendar.getInstance().time)

    suspend fun getTodayReward(): DailyReward {
        val today = todayDate()
        val lastClaimed = dataStore.data.first()[DailyRewardKeys.LAST_CLAIMED_DATE] ?: ""
        val isClaimed = lastClaimed == today

        // Recompensa determinista basada en hash de la fecha — igual para todos los jugadores ese dia
        val seed = today.hashCode().toLong()
        val random = Random(seed)
        val roll = random.nextFloat()

        val tier = when {
            roll < 0.05f -> RewardTier.RARE       // 5%
            roll < 0.30f -> RewardTier.UNCOMMON   // 25%
            else -> RewardTier.COMMON              // 70%
        }

        val xpAmount = when (tier) {
            RewardTier.COMMON -> random.nextInt(10, 31)     // 10-30
            RewardTier.UNCOMMON -> random.nextInt(50, 101)  // 50-100
            RewardTier.RARE -> random.nextInt(200, 501)     // 200-500
        }

        val coinsAmount = when (tier) {
            RewardTier.COMMON -> random.nextInt(5, 16)      // 5-15
            RewardTier.UNCOMMON -> random.nextInt(20, 51)   // 20-50
            RewardTier.RARE -> random.nextInt(50, 151)      // 50-150
        }

        return DailyReward(tier, xpAmount, coinsAmount, isClaimed)
    }

    suspend fun claimReward(): DailyReward {
        val reward = getTodayReward()
        if (!reward.isClaimed) {
            dataStore.edit { prefs ->
                prefs[DailyRewardKeys.LAST_CLAIMED_DATE] = todayDate()
                prefs[DailyRewardKeys.LAST_XP] = reward.xpAmount
                prefs[DailyRewardKeys.LAST_COINS] = reward.coinsAmount
                prefs[DailyRewardKeys.LAST_TIER] = reward.tier.name
            }
        }
        return reward.copy(isClaimed = true)
    }
}
