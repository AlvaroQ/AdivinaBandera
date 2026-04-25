package com.alvaroquintana.adivinabandera.ui.shop

import com.alvaroquintana.adivinabandera.MainDispatcherRule
import com.alvaroquintana.adivinabandera.managers.CurrencyManager
import com.alvaroquintana.adivinabandera.managers.UnlockablesManager
import com.alvaroquintana.domain.cosmetics.CosmeticCategory
import com.alvaroquintana.domain.cosmetics.CosmeticPurchaseResult
import com.alvaroquintana.domain.cosmetics.CosmeticTier
import com.alvaroquintana.domain.cosmetics.CurrencyBalance
import com.alvaroquintana.domain.cosmetics.PlayerCosmetics
import com.alvaroquintana.domain.cosmetics.Unlockable
import com.alvaroquintana.domain.cosmetics.UnlockCondition
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ShopViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var unlockablesManager: UnlockablesManager
    private lateinit var currencyManager: CurrencyManager
    private lateinit var viewModel: ShopViewModel

    private val frameDefault = unlockable(
        id = "frame_default",
        category = CosmeticCategory.PROFILE_FRAME,
        condition = UnlockCondition.Free,
        isDefault = true
    )
    private val frameGold = unlockable(
        id = "frame_gold",
        category = CosmeticCategory.PROFILE_FRAME,
        condition = UnlockCondition.PurchaseWithCoins(price = 200)
    )
    private val frameDiamond = unlockable(
        id = "frame_diamond",
        category = CosmeticCategory.PROFILE_FRAME,
        condition = UnlockCondition.PurchaseWithGems(price = 5)
    )

    private val baseCosmetics = PlayerCosmetics(unlockedIds = setOf("frame_default"))

    @Before
    fun setUp() {
        unlockablesManager = mockk(relaxed = true)
        currencyManager = mockk()

        every { currencyManager.observeBalance() } returns flowOf(CurrencyBalance(coins = 500, gems = 10))
        coEvery { currencyManager.getBalance() } returns CurrencyBalance(coins = 500, gems = 10)
        coEvery { unlockablesManager.getPlayerCosmetics() } returns baseCosmetics
        coEvery { unlockablesManager.getCatalogByCategory(CosmeticCategory.PROFILE_FRAME) } returns
            listOf(frameDefault, frameGold, frameDiamond)

        viewModel = ShopViewModel(unlockablesManager, currencyManager)
    }

    @Test
    fun `Load populates state with balance and items for the default category`() = runTest {
        viewModel.dispatch(ShopViewModel.Intent.Load)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.isLoading)
        assertEquals(500, state.balance.coins)
        assertEquals(3, state.items.size)
        // The default frame is owned and equipped
        val defaultItem = state.items.first { it.unlockable.id == "frame_default" }
        assertTrue(defaultItem.isOwned)
        assertTrue(defaultItem.isEquipped)
    }

    @Test
    fun `SelectCategory rebuilds the items list for the new category`() = runTest {
        coEvery { unlockablesManager.getCatalogByCategory(CosmeticCategory.TITLE_BADGE) } returns emptyList()

        viewModel.dispatch(ShopViewModel.Intent.Load)
        advanceUntilIdle()
        viewModel.dispatch(ShopViewModel.Intent.SelectCategory(CosmeticCategory.TITLE_BADGE))
        advanceUntilIdle()

        assertEquals(CosmeticCategory.TITLE_BADGE, viewModel.state.value.selectedCategory)
        assertTrue(viewModel.state.value.items.isEmpty())
    }

    @Test
    fun `TapItem on a purchasable unowned item shows the confirm dialog`() = runTest {
        viewModel.dispatch(ShopViewModel.Intent.Load)
        advanceUntilIdle()
        val goldItem = viewModel.state.value.items.first { it.unlockable.id == "frame_gold" }

        viewModel.dispatch(ShopViewModel.Intent.TapItem(goldItem))
        advanceUntilIdle()

        val state = viewModel.state.value
        assertTrue(state.showPurchaseConfirmDialog)
        assertEquals(goldItem, state.pendingPurchaseItem)
    }

    @Test
    fun `DismissPurchase clears the pending dialog and item`() = runTest {
        viewModel.dispatch(ShopViewModel.Intent.Load)
        advanceUntilIdle()
        val goldItem = viewModel.state.value.items.first { it.unlockable.id == "frame_gold" }
        viewModel.dispatch(ShopViewModel.Intent.TapItem(goldItem))
        advanceUntilIdle()

        viewModel.dispatch(ShopViewModel.Intent.DismissPurchase)
        advanceUntilIdle()

        val state = viewModel.state.value
        assertFalse(state.showPurchaseConfirmDialog)
        assertNull(state.pendingPurchaseItem)
    }

    @Test
    fun `ConfirmPurchase invokes purchaseItem and shows success message`() = runTest {
        coEvery { unlockablesManager.purchaseItem("frame_gold") } returns
            CosmeticPurchaseResult.Success(frameGold, CurrencyBalance(coins = 300, gems = 10))
        coEvery { unlockablesManager.getPlayerCosmetics() } returnsMany listOf(
            baseCosmetics,
            baseCosmetics.copy(unlockedIds = baseCosmetics.unlockedIds + "frame_gold")
        )

        viewModel.dispatch(ShopViewModel.Intent.Load)
        advanceUntilIdle()
        val goldItem = viewModel.state.value.items.first { it.unlockable.id == "frame_gold" }
        viewModel.dispatch(ShopViewModel.Intent.TapItem(goldItem))
        advanceUntilIdle()
        viewModel.dispatch(ShopViewModel.Intent.ConfirmPurchase)
        advanceUntilIdle()

        coVerify { unlockablesManager.purchaseItem("frame_gold") }
        val state = viewModel.state.value
        assertEquals("Obtuviste: ${frameGold.name}", state.purchaseMessage)
        assertFalse(state.showPurchaseConfirmDialog)
    }

    @Test
    fun `DismissPurchaseMessage clears the snackbar message`() = runTest {
        coEvery { unlockablesManager.purchaseItem(any()) } returns
            CosmeticPurchaseResult.Success(frameGold, CurrencyBalance(coins = 300, gems = 10))

        viewModel.dispatch(ShopViewModel.Intent.Load)
        advanceUntilIdle()
        val goldItem = viewModel.state.value.items.first { it.unlockable.id == "frame_gold" }
        viewModel.dispatch(ShopViewModel.Intent.TapItem(goldItem))
        viewModel.dispatch(ShopViewModel.Intent.ConfirmPurchase)
        advanceUntilIdle()

        viewModel.dispatch(ShopViewModel.Intent.DismissPurchaseMessage)
        advanceUntilIdle()

        assertNull(viewModel.state.value.purchaseMessage)
    }

    private fun unlockable(
        id: String,
        category: CosmeticCategory,
        condition: UnlockCondition,
        isDefault: Boolean = false
    ): Unlockable = Unlockable(
        id = id,
        name = id,
        description = "",
        category = category,
        tier = CosmeticTier.COMMON,
        unlockCondition = condition,
        isDefault = isDefault
    )
}
