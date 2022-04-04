package com.alvaroquintana.adivinabandera.ui.select

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alvaroquintana.adivinabandera.common.ScopedViewModel
import com.alvaroquintana.adivinabandera.managers.Analytics

class SelectViewModel : ScopedViewModel() {

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    init {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_SELECT_GAME)
    }

    fun navigateToGame() {
        _navigation.value = Navigation.Game
    }

    fun navigateToLearn() {
        _navigation.value = Navigation.Info
    }

    fun navigateToSettings() {
        _navigation.value = Navigation.Settings
    }

    sealed class Navigation {
        object Game : Navigation()
        object Info : Navigation()
        object Settings : Navigation()
    }
}