package com.alvaroquintana.adivinabandera.ui.settings

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.alvaroquintana.adivinabandera.BuildConfig
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.utils.openAppOnPlayStore
import com.alvaroquintana.adivinabandera.utils.rateApp
import com.alvaroquintana.adivinabandera.utils.shareApp

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // rate_app
        val rateApp: Preference? = findPreference("rate_app")
        rateApp?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            rateApp(requireContext())
            false
        }

        // share
        val share: Preference? = findPreference("share")
        share?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            shareApp(requireContext(), -1)
            false
        }

        // Version
        val version: Preference? = findPreference("version")
        version?.summary = "${getString(R.string.settings_version)} ${BuildConfig.VERSION_NAME} (Build ${BuildConfig.VERSION_CODE})"

        // more_apps
        val moreApps: Preference? = findPreference("more_apps")
        moreApps?.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            openAppOnPlayStore(requireContext())
            false
        }
    }

}