package com.alvaroquintana.adivinabandera.ui.info

import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.domain.Country
import com.alvaroquintana.usecases.GetCountryList
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class InfoViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getCountryList: GetCountryList
    private lateinit var viewModel: InfoViewModel

    private val page0Countries = mutableListOf(
        Country(name = "Argentina", alpha2Code = "AR"),
        Country(name = "Brasil", alpha2Code = "BR")
    )
    private val page1Countries = mutableListOf(
        Country(name = "Chile", alpha2Code = "CL"),
        Country(name = "Uruguay", alpha2Code = "UY")
    )

    @Before
    fun setUp() {
        getCountryList = mockk()
        coEvery { getCountryList.invoke(0) } returns page0Countries
        viewModel = InfoViewModel(getCountryList)
    }

    @Test
    fun `LoadPage intent accumulates pages in state`() = runTest {
        coEvery { getCountryList.invoke(1) } returns page1Countries

        viewModel.dispatch(InfoViewModel.Intent.LoadPage(0))
        viewModel.dispatch(InfoViewModel.Intent.LoadPage(1))
        advanceUntilIdle()

        val emitted = viewModel.state.value.countryList
        assertTrue(emitted.containsAll(page0Countries))
        assertTrue(emitted.containsAll(page1Countries))
    }

    @Test
    fun `LoadPage accumulates total size after multiple pages`() = runTest {
        coEvery { getCountryList.invoke(1) } returns page1Countries

        viewModel.dispatch(InfoViewModel.Intent.LoadPage(0))
        viewModel.dispatch(InfoViewModel.Intent.LoadPage(1))
        advanceUntilIdle()

        val emitted = viewModel.state.value.countryList
        assertEquals(page0Countries.size + page1Countries.size, emitted.size)
    }

    @Test
    fun `LoadPage ignores duplicated page requests`() = runTest {
        coEvery { getCountryList.invoke(1) } returns page1Countries

        viewModel.dispatch(InfoViewModel.Intent.LoadPage(0))
        viewModel.dispatch(InfoViewModel.Intent.LoadPage(1))
        viewModel.dispatch(InfoViewModel.Intent.LoadPage(1))
        advanceUntilIdle()

        coVerify(exactly = 1) { getCountryList.invoke(1) }
        assertEquals(page0Countries.size + page1Countries.size, viewModel.state.value.countryList.size)
    }
}
