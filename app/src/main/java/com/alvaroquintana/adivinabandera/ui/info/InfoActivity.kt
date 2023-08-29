package com.alvaroquintana.adivinabandera.ui.info

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.common.viewBinding
import com.alvaroquintana.adivinabandera.databinding.InfoActivityBinding
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import com.alvaroquintana.adivinabandera.utils.showBanner
import com.alvaroquintana.adivinabandera.utils.showBonificado
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics

class InfoActivity : BaseActivity() {
    private val infoBinding by viewBinding(InfoActivityBinding::inflate)
    private var rewardedAd: RewardedAd? = null
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(infoBinding.root)

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

        infoBinding.appBar.btnBack.setSafeOnClickListener { finishAfterTransition() }
        infoBinding.appBar.toolbarTitle.text = getString(R.string.info_title)
        infoBinding.appBar.layoutLife.visibility = View.GONE
    }

    fun showAd(show: Boolean){
        showBanner(show, infoBinding.adViewInfo)
    }

    fun showRewardedAd(show: Boolean){
        showBonificado(this, show, rewardedAd)
    }
}