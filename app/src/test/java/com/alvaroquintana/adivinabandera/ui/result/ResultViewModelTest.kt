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

        viewModel = ResultViewModel(
            saveTopScore,
            getRecordScore,
            preferencesDataSource,
            processGameResultUseCase,
            gameStatsManager = mockk(relaxed = true)
        )
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `LoadPersonalRecord saves new record when higher`() = runTest {
        coEvery { preferencesDataSource.getPersonalRecord(any()) } returns 10
        coEvery { preferencesDataSource.savePersonalRecord(any(), any()) } just runs

        viewModel.initWithGameMode("Classic")
        viewModel.dispatch(ResultViewModel.Intent.LoadPersonalRecord(20))

        coVerify { preferencesDataSource.savePersonalRecord(20, "Classic") }
        assertEquals(20, viewModel.state.value.personalRecord)
    }

    @Test
    fun `LoadPersonalRecord does not save when lower`() = runTest {
        coEvery { preferencesDataSource.getPersonalRecord(any()) } returns 50

        viewModel.initWithGameMode("Classic")
        viewModel.dispatch(ResultViewModel.Intent.LoadPersonalRecord(10))

        coVerify(exactly = 0) { preferencesDataSource.savePersonalRecord(any(), any()) }
        assertEquals(50, viewModel.state.value.personalRecord)
    }

    @Test
    fun `LoadPersonalRecord uses correct gameMode key`() = runTest {
        coEvery { preferencesDataSource.getPersonalRecord("CapitalByFlag") } returns 5
        coEvery { preferencesDataSource.savePersonalRecord(any(), any()) } just runs

        viewModel.initWithGameMode("CapitalByFlag")
        viewModel.dispatch(ResultViewModel.Intent.LoadPersonalRecord(15))

        coVerify { preferencesDataSource.savePersonalRecord(15, "CapitalByFlag") }
    }

    @Test
    fun `NavigateToGame intent emits Event Game`() = runTest {
        viewModel.events.test {
            viewModel.dispatch(ResultViewModel.Intent.NavigateToGame)
            assertEquals(ResultViewModel.Event.Game, awaitItem())
        }
    }

    @Test
    fun `NavigateToRate intent emits Event Rate`() = runTest {
        viewModel.events.test {
            viewModel.dispatch(ResultViewModel.Intent.NavigateToRate)
            assertEquals(ResultViewModel.Event.Rate, awaitItem())
        }
    }

    @Test
    fun `NavigateToRanking intent emits Event Ranking`() = runTest {
        viewModel.events.test {
            viewModel.dispatch(ResultViewModel.Intent.NavigateToRanking)
            assertEquals(ResultViewModel.Event.Ranking, awaitItem())
        }
    }

    @Test
    fun `NavigateToShare intent emits Event Share with points`() = runTest {
        viewModel.events.test {
            viewModel.dispatch(ResultViewModel.Intent.NavigateToShare(42))
            assertEquals(ResultViewModel.Event.Share(42), awaitItem())
        }
    }
}
