package com.alvaroquintana.adivinabandera.ui.leaderboard

import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.domain.XpLeaderboardEntry
import com.alvaroquintana.usecases.GetXpLeaderboardUseCase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class XpLeaderboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var getXpLeaderboardUseCase: GetXpLeaderboardUseCase
    private lateinit var viewModel: XpLeaderboardViewModel

    @Before
    fun setUp() {
        mockkStatic(FirebaseCrashlytics::class)
        val crashlytics = mockk<FirebaseCrashlytics>(relaxed = true)
        every { FirebaseCrashlytics.getInstance() } returns crashlytics

        getXpLeaderboardUseCase = mockk()
        viewModel = XpLeaderboardViewModel(getXpLeaderboardUseCase)
    }

    @After
    fun tearDown() {
        unmockkStatic(FirebaseCrashlytics::class)
    }

    @Test
    fun `Load populates state with leaderboard entries on success`() = runTest {
        val entries = listOf(
            XpLeaderboardEntry(uid = "u1", nickname = "Alice", totalXp = 9_999, level = 30),
            XpLeaderboardEntry(uid = "u2", nickname = "Bob", totalXp = 5_000, level = 18)
        )
        coEvery { getXpLeaderboardUseCase.invoke(100) } returns entries

        viewModel.dispatch(XpLeaderboardViewModel.Intent.Load)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(entries, state.entries)
    }

    @Test
    fun `Load surfaces an empty list when the use case throws`() = runTest {
        coEvery { getXpLeaderboardUseCase.invoke(any()) } throws RuntimeException("network down")

        viewModel.dispatch(XpLeaderboardViewModel.Intent.Load)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertTrue(state.entries.isEmpty())
    }
}
