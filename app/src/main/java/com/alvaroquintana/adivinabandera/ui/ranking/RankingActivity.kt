package com.alvaroquintana.adivinabandera.ui.ranking

import android.os.Bundle
import android.view.View
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.common.viewBinding
import com.alvaroquintana.adivinabandera.databinding.RankingActivityBinding
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener

class RankingActivity : BaseActivity() {
    private val rankingBinding by viewBinding(RankingActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(rankingBinding.root)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, RankingFragment.newInstance())
                .commitNow()
        }

        rankingBinding.appBar.btnBack.setSafeOnClickListener {
            finish()
        }
        rankingBinding.appBar.toolbarTitle.text = getString(R.string.ranking_screen_title)
        rankingBinding.appBar.layoutLife.visibility = View.GONE
    }
}