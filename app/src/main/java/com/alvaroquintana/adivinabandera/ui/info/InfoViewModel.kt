package com.alvaroquintana.adivinabandera.ui.info

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.alvaroquintana.adivinabandera.common.ScopedViewModel
import com.alvaroquintana.adivinabandera.ui.game.GameViewModel
import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryList
import kotlinx.coroutines.launch

class InfoViewModel(private val getCountryList: GetCountryList) : ScopedViewModel() {
    private var list = mutableListOf<Country>()

    private val _progress = MutableLiveData<UiModel>()
    val progress: LiveData<UiModel> = _progress

    private val _navigation = MutableLiveData<Navigation>()
    val navigation: LiveData<Navigation> = _navigation

    private val _prideList = MutableLiveData<MutableList<Country>>()
    val prideList: LiveData<MutableList<Country>> = _prideList

    private val _updatePrideList = MutableLiveData<MutableList<Country>>()
    val updatePrideList: LiveData<MutableList<Country>> = _updatePrideList

    private val _showingAds = MutableLiveData<UiModel>()
    val showingAds: LiveData<UiModel> = _showingAds

    init {
        launch {
            _progress.value = UiModel.Loading(true)
            _prideList.value = getCountryList(0)
            _showingAds.value = UiModel.ShowAd(true)
            _progress.value = UiModel.Loading(false)
        }
    }

    fun loadMorePrideList(currentPage: Int) {
        launch {
            _progress.value = UiModel.Loading(true)
            _updatePrideList.value = getCountryList(currentPage)
            _progress.value = UiModel.Loading(false)
        }
    }

    private suspend fun getCountryList(currentPage: Int): MutableList<Country> {
        list = (list + getCountryList.invoke(currentPage)) as MutableList<Country>
        return list
    }

    fun navigateToSelect() {
        _navigation.value = Navigation.Select
    }

    fun showRewardedAd() {
        _showingAds.value = UiModel.ShowReewardAd(true)
    }

    sealed class Navigation {
        object Select : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowAd(val show: Boolean) : UiModel()
        data class ShowReewardAd(val show: Boolean) : UiModel()
    }
}