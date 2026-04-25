package com.alvaroquintana.adivinabandera.ui.info

import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryList
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey

data class InfoUiState(
    val isLoading: Boolean = false,
    val countryList: List<Country> = emptyList()
)

@ContributesIntoMap(AppScope::class)
@ViewModelKey(InfoViewModel::class)
@Inject
class InfoViewModel(
    private val getCountryList: GetCountryList
) : MviViewModel<InfoUiState, InfoViewModel.Intent, InfoViewModel.Event>(InfoUiState()) {

    sealed class Intent {
        /** Loads a specific page. The screen fires LoadPage(0) on first composition. */
        data class LoadPage(val page: Int) : Intent()
    }

    /** No one-shot side effects on this screen — kept for the type parameter. */
    sealed class Event

    private val list = mutableListOf<Country>()
    private val loadedPages = mutableSetOf<Int>()
    private var isLoadingPage = false
    private var endReached = false

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            is Intent.LoadPage -> loadPage(intent.page)
        }
    }

    private suspend fun loadPage(currentPage: Int) {
        if (currentPage < 0 || isLoadingPage || endReached || loadedPages.contains(currentPage)) return

        isLoadingPage = true
        updateState { it.copy(isLoading = true) }
        try {
            val page = getCountryList.invoke(currentPage)
            if (page.isEmpty()) {
                endReached = true
                return
            }

            loadedPages.add(currentPage)
            appendUniqueCountries(page)
            updateState { it.copy(countryList = list.toList()) }
        } finally {
            isLoadingPage = false
            updateState { it.copy(isLoading = false) }
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
}
