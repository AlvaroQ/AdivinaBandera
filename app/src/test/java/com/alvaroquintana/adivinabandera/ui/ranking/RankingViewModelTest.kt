package com.alvaroquintana.adivinabandera.ui.ranking

import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.domain.User
import com.alvaroquintana.usecases.GetRankingScore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getRankingScore: GetRankingScore
    private lateinit var viewModel: RankingViewModel

    @Before
    fun setUp() {
        mockkObject(Analytics)
        every { Analytics.analyticsScreenViewed(any()) } just runs
        getRankingScore = mockk()
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `Load intent populates state with ranking entries`() = runTest {
        val expectedList = mutableListOf(
            User(name = "Alice", points = "50", score = 50),
            User(name = "Bob", points = "30", score = 30)
        )
        coEvery { getRankingScore.invoke(any()) } returns expectedList

        viewModel = RankingViewModel(getRankingScore)
        viewModel.dispatch(RankingViewModel.Intent.Load)
        advanceUntilIdle()

        assertEquals(expectedList, viewModel.state.value.entries)
    }

    @Test
    fun `Load intent toggles isLoading false after fetching`() = runTest {
        coEvery { getRankingScore.invoke(any()) } returns mutableListOf()

        viewModel = RankingViewModel(getRankingScore)
        viewModel.dispatch(RankingViewModel.Intent.Load)
        advanceUntilIdle()

        assertFalse(viewModel.state.value.isLoading)
    }
}
