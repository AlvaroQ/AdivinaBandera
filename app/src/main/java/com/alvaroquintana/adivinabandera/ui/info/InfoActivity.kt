package com.alvaroquintana.adivinabandera.ui.info

import android.app.Activity
import android.os.Bundle
import android.view.View
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.info_activity.*

class InfoActivity : BaseActivity() {
    private lateinit var rewardedAd: RewardedAd
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

        btnBack.setSafeOnClickListener { finishAfterTransition() }
        toolbarTitle.text = getString(R.string.info_title)
        layoutLife.visibility = View.GONE
    }

    fun showAd(){
        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        adViewInfo.loadAd(adRequest)
    }

    fun showRewardedAd(show: Boolean){
        if(show) {
            rewardedAd = RewardedAd(this, getString(R.string.BONIFICADO_GAME))
            val adLoadCallback: RewardedAdLoadCallback = object : RewardedAdLoadCallback() {
                override fun onRewardedAdLoaded() {
                    rewardedAd.show(activity, null)
                }

                override fun onRewardedAdFailedToLoad(adError: LoadAdError) {
                    FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
                }
            }
            rewardedAd.loadAd(AdRequest.Builder().build(), adLoadCallback)
        }
    }
}