package com.alvaroquintana.adivinabandera.ui.info

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import com.alvaroquintana.adivinabandera.utils.showBanner
import com.alvaroquintana.adivinabandera.utils.showBonificado
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.game_activity.adViewGame
import kotlinx.android.synthetic.main.info_activity.adViewInfo

class InfoActivity : BaseActivity() {
    private var rewardedAd: RewardedAd? = null
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.info_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerInfo, InfoFragment.newInstance())
                .commitNow()
        }
        activity = this

        MobileAds.initialize(this)
        RewardedAd.load(this, getString(R.string.BONIFICADO_GAME), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("InfoActivity", adError.toString())
                FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("InfoActivity", "Ad was loaded.")
                rewardedAd = ad
            }
        })

        btnBack.setSafeOnClickListener { finishAfterTransition() }
        toolbarTitle.text = getString(R.string.info_title)
        layoutLife.visibility = View.GONE
    }

    fun showAd(show: Boolean){
        showBanner(show, adViewInfo)
    }

    fun showRewardedAd(show: Boolean){
        showBonificado(this, show, rewardedAd)
    }
}