package com.alvaroquintana.adivinabandera.ui.select

import android.os.Bundle
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.common.viewBinding
import com.alvaroquintana.adivinabandera.databinding.SelectActivityBinding

class SelectActivity : BaseActivity() {
    private val selectBinding by viewBinding(SelectActivityBinding::inflate)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(selectBinding.root)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.containerSelect, SelectFragment.newInstance())
                .commitNow()
        }
    }
}