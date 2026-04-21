package com.alvaroquintana.adivinabandera.managers

import android.content.Context
import android.os.Bundle
import com.alvaroquintana.adivinabandera.BuildConfig
import com.google.firebase.analytics.FirebaseAnalytics

object Analytics {
    lateinit var mFirebase: FirebaseAnalytics

    fun initialize(ctx: Context) {
        mFirebase = FirebaseAnalytics.getInstance(ctx.applicationContext)
    }

    // region Screen Events

    fun analyticsScreenViewed(screenTitle: String) {
        logEvent(Event("screen_viewed")
            .with("screen_title", screenTitle)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    // endregion

    // region Game Events

    fun analyticsGameFinished(points: String) {
        logEvent(Event("game_finished")
            .with("points", points)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsGameAnswer(isCorrect: Boolean, stage: Int) {
        logEvent(Event("game_answer")
            .with("is_correct", isCorrect.toString())
            .with("stage", stage.toString())
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    // endregion

    // region Ad Events

    fun analyticsAdImpression(adType: String, adLocation: String) {
        logEvent(Event("ad_impression")
            .with("ad_type", adType)
            .with("ad_location", adLocation)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsAdRewardEarned(adLocation: String) {
        logEvent(Event("ad_reward_earned")
            .with("ad_location", adLocation)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsAdFailedToLoad(adType: String, adLocation: String, errorMessage: String) {
        logEvent(Event("ad_failed_to_load")
            .with("ad_type", adType)
            .with("ad_location", adLocation)
            .with("error_message", errorMessage)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    // endregion

    // region Streak Events

    fun analyticsStreakUpdated(streakDays: Int, eventType: String) {
        logEvent(Event("streak_updated")
            .with("streak_days", streakDays.toString())
            .with("event_type", eventType) // continued, broken, saved, new
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsStreakMilestone(streakDays: Int) {
        logEvent(Event("streak_milestone")
            .with("milestone_days", streakDays.toString())
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    // endregion

    // region Challenge Events

    fun analyticsChallengeCompleted(challengeId: String, difficulty: String) {
        logEvent(Event("challenge_completed")
            .with("challenge_id", challengeId)
            .with("difficulty", difficulty)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsAllDailyChallengesCompleted() {
        logEvent(Event("all_daily_challenges_completed")
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    // endregion

    // region Click Events

    fun analyticsClicked(btnDescription: String) {
        logEvent(Event("clicked")
            .with("component", btnDescription)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    fun analyticsAppRecommendedOpen(appName: String) {
        logEvent(Event("app_recommended_open")
            .with("recommended_app", appName)
            .with("app_version", BuildConfig.VERSION_NAME)
            .with("app_name", BuildConfig.APPLICATION_ID))
    }

    // endregion

    private fun logEvent(event: Event) {
        mFirebase.logEvent(event.eventName, event.bundle)
    }

    private class Event(val eventName: String) {
        val bundle = Bundle()
        fun with(key: String, value: String): Event {
            bundle.putString(key, value)
            return this
        }
    }

    // Screens
    const val SCREEN_GAME = "screen_game"
    const val SCREEN_RESULT = "screen_result"
    const val SCREEN_RANKING = "screen_ranking"
    const val SCREEN_SELECT_GAME = "screen_select_game"
    const val SCREEN_DIALOG_SAVE_SCORE = "screen_dialog_save_score"
    const val SCREEN_INFO = "screen_info"
    const val SCREEN_SETTINGS = "screen_settings"
    const val SCREEN_PROFILE = "screen_profile"
    const val SCREEN_XP_LEADERBOARD = "screen_xp_leaderboard"

    // Ad Types
    const val AD_TYPE_BANNER = "banner"
    const val AD_TYPE_REWARDED = "rewarded"

    // Ad Locations
    const val AD_LOC_GAME = "game"
    const val AD_LOC_RANKING = "ranking"
    const val AD_LOC_INFO = "info"

    // Clicked
    const val BTN_PLAY_AGAIN = "btn_play_again"
    const val BTN_RATE = "btn_rate"
    const val BTN_RANKING = "btn_ranking"
    const val BTN_SHARE = "btn_share"
    const val BTN_PLAY_CURRENCY_DETECTIVE = "btn_play_currency_detective"
    const val BTN_PLAY_POPULATION_CHALLENGE = "btn_play_population_challenge"
    const val BTN_PLAY_WORLD_MIX = "btn_play_world_mix"
}
