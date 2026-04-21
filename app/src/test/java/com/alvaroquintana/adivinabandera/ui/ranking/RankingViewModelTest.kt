package com.alvaroquintana.adivinabandera.ui.ranking

import app.cash.turbine.test
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
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test

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
    fun `init loads ranking list`() = runTest {
        val expectedList = mutableListOf(
            User(name = "Alice", points = "50", score = 50),
            User(name = "Bob", points = "30", score = 30)
        )
        coEvery { getRankingScore.invoke(any()) } returns expectedList

        viewModel = RankingViewModel(getRankingScore)

        viewModel.rankingList.test {
            assertEquals(expectedList, awaitItem())
        }
    }

    @Test
    fun `progress emits Loading false after ranking loaded`() = runTest {
        coEvery { getRankingScore.invoke(any()) } returns mutableListOf()

        viewModel = RankingViewModel(getRankingScore)

        viewModel.progress.test {
            val state = awaitItem()
            assertFalse((state as RankingViewModel.UiModel.Loading).show)
        }
    }
}
