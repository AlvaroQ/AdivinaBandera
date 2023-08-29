package com.alvaroquintana.adivinabandera.ui.result

import android.content.Intent
import android.os.Bundle
import android.view.View
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.common.startActivity
import com.alvaroquintana.adivinabandera.common.viewBinding
import com.alvaroquintana.adivinabandera.databinding.ResultActivityBinding
import com.alvaroquintana.adivinabandera.ui.select.SelectActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener

class ResultActivity : BaseActivity() {
    private val resultBinding by viewBinding(ResultActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.result_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerResult, ResultFragment.newInstance())
                .commitNow()
        }

        resultBinding.appBar.btnBack.setSafeOnClickListener {
            startActivity<SelectActivity> {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
        }
        resultBinding.appBar.toolbarTitle.text = getString(R.string.resultado_screen_title)
        resultBinding.appBar.layoutLife.visibility = View.GONE
    }
}