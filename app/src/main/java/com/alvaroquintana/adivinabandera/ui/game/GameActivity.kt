package com.alvaroquintana.adivinabandera.ui.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.common.startActivity
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.select.SelectActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import com.alvaroquintana.adivinabandera.utils.showBanner
import com.alvaroquintana.adivinabandera.utils.showBonificado
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics
import kotlinx.android.synthetic.main.app_bar_layout.*
import kotlinx.android.synthetic.main.game_activity.*


class GameActivity : BaseActivity() {
    private var rewardedAd: RewardedAd? = null
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game_activity)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerGame, GameFragment.newInstance())
                .commitNow()
        }
        activity = this

        MobileAds.initialize(this)
        RewardedAd.load(this, getString(R.string.BONIFICADO_GAME), AdRequest.Builder().build(), object : RewardedAdLoadCallback() {
            override fun onAdFailedToLoad(adError: LoadAdError) {
                Log.d("GameActivity", adError.toString())
                FirebaseCrashlytics.getInstance().recordException(Throwable(adError.message))
                rewardedAd = null
            }

            override fun onAdLoaded(ad: RewardedAd) {
                Log.d("GameActivity", "Ad was loaded.")
                rewardedAd = ad
            }
        })

        btnBack.setSafeOnClickListener {
            startActivity<SelectActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        writeStage(1)
    }

    fun writeStage(stage: Int) {
        toolbarTitle.text = stage.toString()
    }

    fun writeDeleteLife(life: Int) {
        when(life) {
            3 -> {
                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_on))
            }
            2 -> {
                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
            1 -> {
                lifeSecond.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))

                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
            0 -> {
                lifeFirst.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))

                // GAME OVER
                lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
        }
    }

    fun showBannerAd(show: Boolean){
        showBanner(show, adViewGame)
    }

    fun showRewardedAd(show: Boolean){
        showBonificado(this, show, rewardedAd)
    }
}