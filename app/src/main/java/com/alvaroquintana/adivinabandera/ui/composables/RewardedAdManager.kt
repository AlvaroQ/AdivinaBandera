package com.alvaroquintana.adivinabandera.ui.composables

import android.app.Activity
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.alvaroquintana.adivinabandera.BuildConfig
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics

class RewardedAdState(
    private val activity: Activity,
    private val adUnitId: String,
    private val adLocation: String
) {
    var rewardedAd: RewardedAd? = null
        private set

    private var isLoading = false
    private var retryCount = 0
    private val maxRetries = 2

    private fun shouldReportAdError(code: Int): Boolean {
        return code != AdRequest.ERROR_CODE_NO_FILL && code != AdRequest.ERROR_CODE_NETWORK_ERROR
    }

    fun load() {
        if (isLoading || rewardedAd != null) return
        isLoading = true
        FirebaseCrashlytics.getInstance().log("rewarded_ad_load_started:$adLocation")

        RewardedAd.load(
            activity,
            adUnitId,
            AdRequest.Builder().build(),
            object : RewardedAdLoadCallback() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    if (BuildConfig.DEBUG) Log.d("RewardedAdState", "Failed to load: $adError")
                    FirebaseCrashlytics.getInstance().apply {
                        log("rewarded_ad_load_failed:$adLocation:${adError.code}")
                        setCustomKey("ad_location", adLocation)
                        setCustomKey("ad_error_code", adError.code)
                        if (shouldReportAdError(adError.code)) {
                            recordException(Exception("RewardedAd load failed: ${adError.message}"))
                        }
                    }
                    Analytics.analyticsAdFailedToLoad(
                        Analytics.AD_TYPE_REWARDED, adLocation, adError.message
                    )
                    rewardedAd = null
                    isLoading = false

                    if (retryCount < maxRetries) {
                        retryCount++
                        load()
                    }
                }

                override fun onAdLoaded(ad: RewardedAd) {
                    if (BuildConfig.DEBUG) Log.d("RewardedAdState", "Ad was loaded for $adLocation")
                    FirebaseCrashlytics.getInstance().log("rewarded_ad_load_success:$adLocation")
                    rewardedAd = ad
                    isLoading = false
                    retryCount = 0
                }
            }
        )
    }

    fun show() {
        val ad = rewardedAd
        if (ad != null) {
            FirebaseCrashlytics.getInstance().log("rewarded_ad_show_requested:$adLocation")
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdShowedFullScreenContent() {
                    FirebaseCrashlytics.getInstance().log("rewarded_ad_show_success:$adLocation")
                    Analytics.analyticsAdImpression(Analytics.AD_TYPE_REWARDED, adLocation)
                }

                override fun onAdDismissedFullScreenContent() {
                    FirebaseCrashlytics.getInstance().log("rewarded_ad_dismissed:$adLocation")
                    rewardedAd = null
                    load()
                }

                override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                    if (BuildConfig.DEBUG) Log.d("RewardedAdState", "Failed to show: $adError")
                    FirebaseCrashlytics.getInstance().apply {
                        log("rewarded_ad_show_failed:$adLocation:${adError.code}")
                        if (shouldReportAdError(adError.code)) {
                            recordException(Exception("RewardedAd show failed: ${adError.message}"))
                        }
                    }
                    rewardedAd = null
                    load()
                }
            }

            ad.show(activity) { rewardItem ->
                if (BuildConfig.DEBUG) Log.d(
                    "RewardedAdState",
                    "User earned reward: amount=${rewardItem.amount}, type=${rewardItem.type}"
                )
                Analytics.analyticsAdRewardEarned(adLocation)
            }
        } else {
            if (BuildConfig.DEBUG) Log.d("RewardedAdState", "The rewarded ad wasn't ready yet, reloading.")
            FirebaseCrashlytics.getInstance().log("rewarded_ad_not_ready_reloading:$adLocation")
            load()
        }
    }
}

@Composable
fun rememberRewardedAdState(adUnitId: String, adLocation: String): RewardedAdState {
    val context = LocalContext.current
    val activity = context as Activity
    return remember(adUnitId) {
        RewardedAdState(activity, adUnitId, adLocation).also { it.load() }
    }
}
