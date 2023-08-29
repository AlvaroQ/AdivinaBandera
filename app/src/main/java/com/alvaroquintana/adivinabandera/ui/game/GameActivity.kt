package com.alvaroquintana.adivinabandera.ui.game

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.AnimationUtils
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.common.startActivity
import com.alvaroquintana.adivinabandera.common.viewBinding
import com.alvaroquintana.adivinabandera.databinding.GameActivityBinding
import com.alvaroquintana.adivinabandera.ui.select.SelectActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import com.alvaroquintana.adivinabandera.utils.showBanner
import com.alvaroquintana.adivinabandera.utils.showBonificado
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.firebase.crashlytics.FirebaseCrashlytics


class GameActivity : BaseActivity() {
    private val gameBinding by viewBinding(GameActivityBinding::inflate)
    private var rewardedAd: RewardedAd? = null
    private lateinit var activity: Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(gameBinding.root)

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

        gameBinding.appBar.btnBack.setSafeOnClickListener {
            startActivity<SelectActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }

        writeStage(1)
    }

    fun writeStage(stage: Int) {
        gameBinding.appBar.toolbarTitle.text = stage.toString()
    }

    fun writeDeleteLife(life: Int) {
        when(life) {
            3 -> {
                gameBinding.appBar.lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                gameBinding.appBar.lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                gameBinding.appBar.lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_on))
            }
            2 -> {
                gameBinding.appBar.lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                gameBinding.appBar.lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                gameBinding.appBar.lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
            1 -> {
                gameBinding.appBar.lifeSecond.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))

                gameBinding.appBar.lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_on))
                gameBinding.appBar.lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                gameBinding.appBar.lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
            0 -> {
                gameBinding.appBar.lifeFirst.startAnimation(AnimationUtils.loadAnimation(this, R.anim.scale_xy_collapse))

                // GAME OVER
                gameBinding.appBar.lifeFirst.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                gameBinding.appBar.lifeSecond.setImageDrawable(getDrawable(R.drawable.ic_life_off))
                gameBinding.appBar.lifeThree.setImageDrawable(getDrawable(R.drawable.ic_life_off))
            }
        }
    }

    fun showBannerAd(show: Boolean){
        showBanner(show, gameBinding.adViewGame)
    }

    fun showRewardedAd(show: Boolean){
        showBonificado(this, show, rewardedAd)
    }
}