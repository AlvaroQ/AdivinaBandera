package com.alvaroquintana.adivinabandera.common

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey

object DataStoreKeys {

    // Progresion
    object ProgressionKeys {
        val TOTAL_XP = intPreferencesKey("total_xp")
        val CURRENT_LEVEL = intPreferencesKey("current_level")
        val CURRENT_TITLE = stringPreferencesKey("current_title")
        val NICKNAME = stringPreferencesKey("nickname")
        val IMAGE_BASE64 = stringPreferencesKey("image_base64")
    }

    // Estadisticas de juego
    object GameStatsKeys {
        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        val TOTAL_CORRECT_ANSWERS = intPreferencesKey("total_correct_answers")
        val TOTAL_WRONG_ANSWERS = intPreferencesKey("total_wrong_answers")
        val BEST_STREAK_EVER = intPreferencesKey("best_streak_ever")
        val TOTAL_PERFECT_GAMES = intPreferencesKey("total_perfect_games")
        val TOTAL_TIME_PLAYED_MS = longPreferencesKey("total_time_played_ms")
    }

    // Logros
    object AchievementKeys {
        val UNLOCKED_ACHIEVEMENTS = stringSetPreferencesKey("unlocked_achievements")
    }

    // Sincronizacion XP
    object XpSyncKeys {
        val PENDING_SYNC = booleanPreferencesKey("pending_xp_sync")
        val LAST_SYNC_TIMESTAMP = longPreferencesKey("last_xp_sync_timestamp")
    }

    // Preferencias (migradas de SharedPreferences)
    object PreferencesKeys {
        val PERSONAL_RECORD_CLASSIC = intPreferencesKey("personal_record_classic")
        val PERSONAL_RECORD_CAPITAL_BY_FLAG = intPreferencesKey("personal_record_capital_by_flag")
        val PERSONAL_RECORD_CURRENCY_DETECTIVE = intPreferencesKey("personal_record_currency_detective")
        val PERSONAL_RECORD_POPULATION_CHALLENGE = intPreferencesKey("personal_record_population_challenge")
        val PERSONAL_RECORD_WORLD_MIX = intPreferencesKey("personal_record_world_mix")
        val PERSONAL_RECORD_REGIONAL = intPreferencesKey("personal_record_regional")
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    // Progresion de modos regionales: aciertos acumulados por pais.
    object RegionalProgressionKeys {
        val CORRECT_ANSWERS_ES = intPreferencesKey("regional_correct_es")
        val CORRECT_ANSWERS_MX = intPreferencesKey("regional_correct_mx")
        val CORRECT_ANSWERS_AR = intPreferencesKey("regional_correct_ar")
        val CORRECT_ANSWERS_BR = intPreferencesKey("regional_correct_br")
        val CORRECT_ANSWERS_DE = intPreferencesKey("regional_correct_de")
        val CORRECT_ANSWERS_US = intPreferencesKey("regional_correct_us")
    }

    // Tema
    object ThemeKeys {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val DYNAMIC_COLORS = booleanPreferencesKey("dynamic_colors")
    }

    // Rachas diarias
    object StreakKeys {
        val CURRENT_STREAK = intPreferencesKey("streak_current")
        val BEST_STREAK = intPreferencesKey("streak_best")
        val LAST_PLAYED_DATE = stringPreferencesKey("streak_last_played_date")
        val FREEZE_TOKENS = intPreferencesKey("streak_freeze_tokens")
        val CYCLE_DAY = intPreferencesKey("streak_cycle_day")
        val TOTAL_DAYS_PLAYED = intPreferencesKey("streak_total_days_played")
        val STREAK_START_DATE = stringPreferencesKey("streak_start_date")
        val LAST_FREEZE_USED_DATE = stringPreferencesKey("streak_last_freeze_used_date")
    }

    // Recompensa diaria
    object DailyRewardKeys {
        val LAST_CLAIMED_DATE = stringPreferencesKey("daily_reward_last_claimed_date")
        val LAST_XP = intPreferencesKey("daily_reward_last_xp")
        val LAST_COINS = intPreferencesKey("daily_reward_last_coins")
        val LAST_TIER = stringPreferencesKey("daily_reward_last_tier")
    }

    // Estadisticas semanales
    object WeeklyStatsKeys {
        val START_DATE = stringPreferencesKey("weekly_stats_start_date")
        val START_XP = intPreferencesKey("weekly_stats_start_xp")
        val CORRECT_ANSWERS = intPreferencesKey("weekly_stats_correct_answers")
        val NEW_ACHIEVEMENTS = intPreferencesKey("weekly_stats_new_achievements")
    }

    // Preferencias de notificaciones
    object NotificationKeys {
        val DAILY_REMINDER_ENABLED = booleanPreferencesKey("notification_daily_reminder_enabled")
        val DAILY_REMINDER_HOUR = intPreferencesKey("notification_daily_reminder_hour")
        val DAILY_REMINDER_MINUTE = intPreferencesKey("notification_daily_reminder_minute")
    }
}
