package com.alvaroquintana.adivinabandera.ui.composables

import android.widget.LinearLayout
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.viewinterop.AndroidView
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.firebase.crashlytics.FirebaseCrashlytics

@Composable
fun AdBannerView(
    adUnitId: String,
    modifier: Modifier = Modifier,
    adLocation: String = Analytics.AD_LOC_GAME
) {
    fun shouldReportAdError(code: Int): Boolean {
        return code != AdRequest.ERROR_CODE_NO_FILL && code != AdRequest.ERROR_CODE_NETWORK_ERROR
    }

    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp

    AndroidView(
        modifier = modifier,
        factory = { context ->
            AdView(context).apply {
                setAdSize(AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, screenWidthDp))
                this.adUnitId = adUnitId
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                adListener = object : AdListener() {
                    override fun onAdLoaded() {
                        FirebaseCrashlytics.getInstance().log("banner_ad_load_success:$adLocation")
                        Analytics.analyticsAdImpression(Analytics.AD_TYPE_BANNER, adLocation)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        FirebaseCrashlytics.getInstance().apply {
                            log("banner_ad_load_failed:$adLocation:${adError.code}")
                            if (shouldReportAdError(adError.code)) {
                                recordException(Exception("Banner load failed [$adLocation]: ${adError.message}"))
                            }
                        }
                        Analytics.analyticsAdFailedToLoad(
                            Analytics.AD_TYPE_BANNER, adLocation, adError.message
                        )
                    }
                }
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
