package com.alvaroquintana.adivinabandera.ui.info

import app.cash.turbine.test
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
        // init block calls getCountryList(0), so mock page 0 before construction
        coEvery { getCountryList.invoke(0) } returns page0Countries
        viewModel = InfoViewModel(getCountryList)
    }

    @Test
    fun `loadMorePrideList emits accumulated country list`() = runTest {
        coEvery { getCountryList.invoke(1) } returns page1Countries
        advanceUntilIdle()

        viewModel.countryList.test {
            skipItems(1)
            viewModel.loadMorePrideList(1)
            val emitted = awaitItem()
            assertTrue(emitted.containsAll(page0Countries))
            assertTrue(emitted.containsAll(page1Countries))
        }
    }

    @Test
    fun `loadMorePrideList accumulates results on subsequent calls`() = runTest {
        coEvery { getCountryList.invoke(1) } returns page1Countries
        advanceUntilIdle()

        viewModel.countryList.test {
            skipItems(1)
            viewModel.loadMorePrideList(1)
            val emitted = awaitItem()
            // After init loaded page0 (2 items) and loadMore loaded page1 (2 items),
            // the internal list should have accumulated both pages
            assertEquals(page0Countries.size + page1Countries.size, emitted.size)
            assertTrue(emitted.containsAll(page0Countries))
            assertTrue(emitted.containsAll(page1Countries))
        }
    }

    @Test
    fun `loadMorePrideList ignores duplicated page requests`() = runTest {
        coEvery { getCountryList.invoke(1) } returns page1Countries
        advanceUntilIdle()

        viewModel.loadMorePrideList(1)
        viewModel.loadMorePrideList(1)
        advanceUntilIdle()

        coVerify(exactly = 1) { getCountryList.invoke(1) }
        assertEquals(page0Countries.size + page1Countries.size, viewModel.countryList.value.size)
    }

    @Test
    fun `navigateToSelect emits Navigation Select`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToSelect()
            assertEquals(InfoViewModel.Navigation.Select, awaitItem())
        }
    }
}
