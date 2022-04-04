package com.alvaroquintana.adivinabandera.ui.settings

import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.base.BaseActivity
import com.alvaroquintana.adivinabandera.utils.setSafeOnClickListener
import kotlinx.android.synthetic.main.app_bar_layout.*


class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        setupToolbar()
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
            android.R.id.home -> {
                finishAfterTransition()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupToolbar() {
        toolbarTitle.text = getString(R.string.settings)
        layoutLife.visibility = View.GONE
        btnBack.setSafeOnClickListener { finishAfterTransition() }
    }
}