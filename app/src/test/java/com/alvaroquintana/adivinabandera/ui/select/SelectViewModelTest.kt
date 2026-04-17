package com.alvaroquintana.adivinabandera.ui.select

import app.cash.turbine.test
import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.Analytics
import io.mockk.every
import io.mockk.just
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SelectViewModel

    @Before
    fun setUp() {
        mockkObject(Analytics)
        every { Analytics.analyticsScreenViewed(any()) } just runs
        viewModel = SelectViewModel()
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `navigateToGame emits Navigation Game`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToGame()
            assertEquals(SelectViewModel.Navigation.Game, awaitItem())
        }
    }

    @Test
    fun `navigateToLearn emits Navigation Info`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToLearn()
            assertEquals(SelectViewModel.Navigation.Info, awaitItem())
        }
    }

    @Test
    fun `navigateToSettings emits Navigation Settings`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToSettings()
            assertEquals(SelectViewModel.Navigation.Settings, awaitItem())
        }
    }
}
