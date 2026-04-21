package com.alvaroquintana.adivinabandera.ui.info

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryList
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InfoViewModel(private val getCountryList: GetCountryList) : ViewModel() {
    private val list = mutableListOf<Country>()
    private val loadedPages = mutableSetOf<Int>()
    private var isLoadingPage = false
    private var endReached = false

    private val _progress = MutableStateFlow<UiModel>(UiModel.Loading(false))
    val progress: StateFlow<UiModel> = _progress.asStateFlow()

    private val _navigation = MutableSharedFlow<Navigation>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val navigation: SharedFlow<Navigation> = _navigation.asSharedFlow()

    private val _countryList = MutableStateFlow<List<Country>>(emptyList())
    val countryList: StateFlow<List<Country>> = _countryList.asStateFlow()

    private val _showingAds = MutableSharedFlow<UiModel>(replay = 0, extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val showingAds: SharedFlow<UiModel> = _showingAds.asSharedFlow()

    init {
        viewModelScope.launch {
            loadPage(0)
            _showingAds.tryEmit(UiModel.ShowAd(true))
        }
    }

    fun loadMorePrideList(currentPage: Int) {
        if (currentPage < 0 || isLoadingPage || endReached || loadedPages.contains(currentPage)) return

        viewModelScope.launch {
            loadPage(currentPage)
        }
    }

    private suspend fun loadPage(currentPage: Int) {
        isLoadingPage = true
        _progress.value = UiModel.Loading(true)
        try {
            val page = getCountryList.invoke(currentPage)
            if (page.isEmpty()) {
                endReached = true
                return
            }

            loadedPages.add(currentPage)
            appendUniqueCountries(page)
            _countryList.value = list.toList()
        } finally {
            isLoadingPage = false
            _progress.value = UiModel.Loading(false)
        }
    }

    private fun appendUniqueCountries(page: List<Country>) {
        val existingCodes = list
            .map { it.alpha2Code.uppercase() }
            .toMutableSet()

        page.forEach { country ->
            val code = country.alpha2Code.uppercase()
            if (code.isBlank() || existingCodes.add(code)) {
                list.add(country)
            }
        }
    }

    fun navigateToSelect() {
        _navigation.tryEmit(Navigation.Select)
    }

    fun showRewardedAd() {
        _showingAds.tryEmit(UiModel.ShowRewardedAd(true))
    }

    sealed class Navigation {
        object Select : Navigation()
    }

    sealed class UiModel {
        data class Loading(val show: Boolean) : UiModel()
        data class ShowAd(val show: Boolean) : UiModel()
        data class ShowRewardedAd(val show: Boolean) : UiModel()
    }
}
