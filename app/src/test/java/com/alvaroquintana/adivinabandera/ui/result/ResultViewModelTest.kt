package com.alvaroquintana.adivinabandera.ui.result

import app.cash.turbine.test
import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.data.datasource.PreferencesDataSource
import com.alvaroquintana.usecases.GetRecordScore
import com.alvaroquintana.usecases.ProcessGameResultUseCase
import com.alvaroquintana.usecases.SaveTopScore
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ResultViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var saveTopScore: SaveTopScore
    private lateinit var getRecordScore: GetRecordScore
    private lateinit var preferencesDataSource: PreferencesDataSource
    private lateinit var processGameResultUseCase: ProcessGameResultUseCase
    private lateinit var viewModel: ResultViewModel

    @Before
    fun setUp() {
        mockkObject(Analytics)
        every { Analytics.analyticsScreenViewed(any()) } just runs
        every { Analytics.analyticsClicked(any()) } just runs

        saveTopScore = mockk()
        getRecordScore = mockk()
        preferencesDataSource = mockk()
        processGameResultUseCase = mockk()

        coEvery { getRecordScore.invoke(any(), any()) } returns "100"

        viewModel = ResultViewModel(saveTopScore, getRecordScore, preferencesDataSource, processGameResultUseCase)
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `getPersonalRecord saves new record when higher`() = runTest {
        coEvery { preferencesDataSource.getPersonalRecord(any()) } returns 10
        coEvery { preferencesDataSource.savePersonalRecord(any(), any()) } just runs

        viewModel.initWithGameMode("Classic")
        viewModel.getPersonalRecord(20)

        coVerify { preferencesDataSource.savePersonalRecord(20, "Classic") }
        assertEquals("20", viewModel.uiState.value.personalRecord)
    }

    @Test
    fun `getPersonalRecord does not save when lower`() = runTest {
        coEvery { preferencesDataSource.getPersonalRecord(any()) } returns 50

        viewModel.initWithGameMode("Classic")
        viewModel.getPersonalRecord(10)

        coVerify(exactly = 0) { preferencesDataSource.savePersonalRecord(any(), any()) }
        assertEquals("50", viewModel.uiState.value.personalRecord)
    }

    @Test
    fun `getPersonalRecord uses correct gameMode key`() = runTest {
        coEvery { preferencesDataSource.getPersonalRecord("CapitalByFlag") } returns 5
        coEvery { preferencesDataSource.savePersonalRecord(any(), any()) } just runs

        viewModel.initWithGameMode("CapitalByFlag")
        viewModel.getPersonalRecord(15)

        coVerify { preferencesDataSource.savePersonalRecord(15, "CapitalByFlag") }
    }

    @Test
    fun `navigateToGame emits Navigation Game`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToGame()
            assertEquals(ResultViewModel.Navigation.Game, awaitItem())
        }
    }

    @Test
    fun `navigateToRate emits Navigation Rate`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToRate()
            assertEquals(ResultViewModel.Navigation.Rate, awaitItem())
        }
    }

    @Test
    fun `navigateToRanking emits Navigation Ranking`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToRanking()
            assertEquals(ResultViewModel.Navigation.Ranking, awaitItem())
        }
    }

    @Test
    fun `navigateToShare emits Navigation Share with points`() = runTest {
        viewModel.navigation.test {
            viewModel.navigateToShare(42)
            assertEquals(ResultViewModel.Navigation.Share(42), awaitItem())
        }
    }
}
