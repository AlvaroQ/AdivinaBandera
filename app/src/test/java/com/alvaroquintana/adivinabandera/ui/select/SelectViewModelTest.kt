package com.alvaroquintana.adivinabandera.ui.select

import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.managers.CountryMasteryManager
import com.alvaroquintana.adivinabandera.managers.CurrencyManager
import com.alvaroquintana.adivinabandera.managers.DailyChallengeManager
import com.alvaroquintana.adivinabandera.managers.DailyRewardManager
import com.alvaroquintana.adivinabandera.managers.ProgressionManager
import com.alvaroquintana.adivinabandera.managers.RegionalProgressionManager
import com.alvaroquintana.adivinabandera.managers.StreakManager
import com.alvaroquintana.domain.StreakState
import com.alvaroquintana.domain.challenge.DailyChallengeState
import com.alvaroquintana.domain.cosmetics.CurrencyBalance
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.runs
import io.mockk.unmockkObject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SelectViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var streakManager: StreakManager
    private lateinit var dailyChallengeManager: DailyChallengeManager
    private lateinit var progressionManager: ProgressionManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var dailyRewardManager: DailyRewardManager
    private lateinit var countryMasteryManager: CountryMasteryManager
    private lateinit var regionalProgressionManager: RegionalProgressionManager
    private lateinit var viewModel: SelectViewModel

    private val unclaimedReward = DailyRewardManager.DailyReward(
        tier = DailyRewardManager.RewardTier.COMMON,
        xpAmount = 20,
        coinsAmount = 50,
        isClaimed = false
    )
    private val claimedReward = unclaimedReward.copy(isClaimed = true)

    @Before
    fun setUp() {
        mockkObject(Analytics)
        every { Analytics.analyticsScreenViewed(any()) } just runs

        streakManager = mockk()
        dailyChallengeManager = mockk()
        progressionManager = mockk()
        currencyManager = mockk()
        dailyRewardManager = mockk()
        countryMasteryManager = mockk()
        regionalProgressionManager = mockk()

        every { currencyManager.observeBalance() } returns flowOf(CurrencyBalance())

        coEvery { streakManager.getStreakState() } returns StreakState()
        coEvery { streakManager.isStreakAtRisk() } returns false
        coEvery { streakManager.hasPlayedToday() } returns false
        coEvery { dailyChallengeManager.getDailyChallengeState(any()) } returns DailyChallengeState()
        coEvery { progressionManager.getCurrentLevel() } returns 5
        coEvery { progressionManager.getTotalXp() } returns 250
        coEvery { dailyRewardManager.getTodayReward() } returns unclaimedReward
        coEvery { countryMasteryManager.getDiscoveredCount() } returns 42
        coEvery { countryMasteryManager.getWeakSpotsAsIds(any()) } returns listOf(7, 13)
        coEvery { regionalProgressionManager.snapshot() } returns mapOf("ES" to 4)

        viewModel = SelectViewModel(
            streakManager,
            dailyChallengeManager,
            progressionManager,
            currencyManager,
            dailyRewardManager,
            countryMasteryManager,
            regionalProgressionManager
        )
    }

    @After
    fun tearDown() {
        unmockkObject(Analytics)
    }

    @Test
    fun `LoadInitialState populates state with manager snapshot`() = runTest {
        viewModel.dispatch(SelectViewModel.Intent.LoadInitialState)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertEquals(5, state.currentLevel)
        assertEquals(250, state.currentXp)
        assertEquals(42, state.discoveredCountries)
        assertEquals(listOf(7, 13), state.weakSpotCountryIds)
        assertEquals(unclaimedReward, state.dailyReward)
    }

    @Test
    fun `RefreshRegionalProgression only touches regional descriptors`() = runTest {
        viewModel.dispatch(SelectViewModel.Intent.LoadInitialState)
        advanceUntilIdle()
        val before = viewModel.state.value

        coEvery { regionalProgressionManager.snapshot() } returns mapOf("ES" to 6, "MX" to 6)
        viewModel.dispatch(SelectViewModel.Intent.RefreshRegionalProgression)
        advanceUntilIdle()

        val after = viewModel.state.value
        // Non-regional fields are untouched
        assertEquals(before.currentLevel, after.currentLevel)
        assertEquals(before.discoveredCountries, after.discoveredCountries)
        // Regional snapshot moved
        coVerify(atLeast = 2) { regionalProgressionManager.snapshot() }
    }

    @Test
    fun `ClaimDailyReward applies XP and coins when reward is unclaimed`() = runTest {
        coEvery { progressionManager.addXp(any()) } returns Triple(0, 0, false)
        coEvery { currencyManager.earnCoins(any(), any()) } returns CurrencyBalance(coins = 50, gems = 0)
        coEvery { dailyRewardManager.claimReward() } returns claimedReward

        viewModel.dispatch(SelectViewModel.Intent.ClaimDailyReward)
        advanceUntilIdle()

        coVerify { dailyRewardManager.claimReward() }
        coVerify { progressionManager.addXp(20) }
        coVerify { currencyManager.earnCoins(50, "daily_reward") }
        assertEquals(claimedReward, viewModel.state.value.dailyReward)
    }

    @Test
    fun `ClaimDailyReward is a no-op when reward already claimed`() = runTest {
        coEvery { dailyRewardManager.getTodayReward() } returns claimedReward

        viewModel.dispatch(SelectViewModel.Intent.ClaimDailyReward)
        advanceUntilIdle()

        coVerify(exactly = 0) { dailyRewardManager.claimReward() }
        coVerify(exactly = 0) { progressionManager.addXp(any()) }
        coVerify(exactly = 0) { currencyManager.earnCoins(any(), any()) }
    }
}
