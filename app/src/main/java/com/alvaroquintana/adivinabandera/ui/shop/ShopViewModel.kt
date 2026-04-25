package com.alvaroquintana.adivinabandera.ui.shop

import androidx.compose.runtime.Immutable
import androidx.lifecycle.viewModelScope
import com.alvaroquintana.adivinabandera.managers.CurrencyManager
import com.alvaroquintana.adivinabandera.managers.UnlockablesManager
import com.alvaroquintana.adivinabandera.ui.mvi.MviViewModel
import com.alvaroquintana.domain.cosmetics.CosmeticCategory
import com.alvaroquintana.domain.cosmetics.CosmeticPurchaseResult
import com.alvaroquintana.domain.cosmetics.CurrencyBalance
import com.alvaroquintana.domain.cosmetics.PlayerCosmetics
import com.alvaroquintana.domain.cosmetics.Unlockable
import com.alvaroquintana.domain.cosmetics.UnlockCondition
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metrox.viewmodel.ViewModelKey
import kotlinx.coroutines.launch

/**
 * Representa un item del catalogo con estado contextual del jugador.
 */
@Immutable
data class ShopItem(
    val unlockable: Unlockable,
    val isOwned: Boolean,
    val isEquipped: Boolean
)

@Immutable
data class ShopUiState(
    val isLoading: Boolean = true,
    val balance: CurrencyBalance = CurrencyBalance(),
    val selectedCategory: CosmeticCategory = CosmeticCategory.PROFILE_FRAME,
    val items: List<ShopItem> = emptyList(),
    val playerCosmetics: PlayerCosmetics = PlayerCosmetics(),
    val purchaseMessage: String? = null,
    val showPurchaseConfirmDialog: Boolean = false,
    val pendingPurchaseItem: ShopItem? = null
)

@ContributesIntoMap(AppScope::class)
@ViewModelKey(ShopViewModel::class)
@Inject
class ShopViewModel(
    private val unlockablesManager: UnlockablesManager,
    private val currencyManager: CurrencyManager
) : MviViewModel<ShopUiState, ShopViewModel.Intent, ShopViewModel.Event>(ShopUiState()) {

    sealed class Intent {
        object Load : Intent()
        data class SelectCategory(val category: CosmeticCategory) : Intent()
        data class TapItem(val item: ShopItem) : Intent()
        object DismissPurchase : Intent()
        object ConfirmPurchase : Intent()
        data class EquipItem(val item: ShopItem) : Intent()
        object DismissPurchaseMessage : Intent()
    }

    sealed class Event

    init {
        // Currency balance is observed continuously: emissions are streaming
        // and would not fit a single Intent. Stays as an in-init observer.
        observeBalance()
    }

    override suspend fun handleIntent(intent: Intent) {
        when (intent) {
            Intent.Load -> loadShopData()
            is Intent.SelectCategory -> selectCategory(intent.category)
            is Intent.TapItem -> tapItem(intent.item)
            Intent.DismissPurchase -> updateState {
                it.copy(showPurchaseConfirmDialog = false, pendingPurchaseItem = null)
            }
            Intent.ConfirmPurchase -> confirmPurchase()
            is Intent.EquipItem -> equipItem(intent.item)
            Intent.DismissPurchaseMessage -> updateState { it.copy(purchaseMessage = null) }
        }
    }

    private suspend fun loadShopData() {
        updateState { it.copy(isLoading = true) }

        val balance = currencyManager.getBalance()
        val playerCosmetics = unlockablesManager.getPlayerCosmetics()
        val items = buildShopItems(
            category = currentState.selectedCategory,
            playerCosmetics = playerCosmetics
        )

        updateState {
            it.copy(
                isLoading = false,
                balance = balance,
                playerCosmetics = playerCosmetics,
                items = items
            )
        }
    }

    /** Continuous balance observer — drives the header even between Intents. */
    private fun observeBalance() {
        viewModelScope.launch {
            currencyManager.observeBalance().collect { balance ->
                updateState { it.copy(balance = balance) }
            }
        }
    }

    private suspend fun selectCategory(category: CosmeticCategory) {
        val playerCosmetics = currentState.playerCosmetics
        val items = buildShopItems(category, playerCosmetics)
        updateState {
            it.copy(
                selectedCategory = category,
                items = items
            )
        }
    }

    private suspend fun tapItem(shopItem: ShopItem) {
        if (shopItem.isOwned) {
            equipItem(shopItem)
        } else {
            val isPurchasable = shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithCoins ||
                shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithGems
            if (isPurchasable) {
                updateState {
                    it.copy(
                        showPurchaseConfirmDialog = true,
                        pendingPurchaseItem = shopItem
                    )
                }
            }
        }
    }

    private suspend fun confirmPurchase() {
        val item = currentState.pendingPurchaseItem ?: return
        updateState { it.copy(showPurchaseConfirmDialog = false, pendingPurchaseItem = null) }
        purchaseItem(item.unlockable.id)
    }

    private suspend fun purchaseItem(itemId: String) {
        val result = unlockablesManager.purchaseItem(itemId)
        val message = when (result) {
            is CosmeticPurchaseResult.Success -> "Obtuviste: ${result.item.name}"
            is CosmeticPurchaseResult.InsufficientFunds -> {
                val currency = if (result.currency == "coins") "monedas" else "gemas"
                "Faltan ${result.needed - result.have} $currency"
            }
            is CosmeticPurchaseResult.AlreadyOwned -> "Ya tenes este item"
            is CosmeticPurchaseResult.ConditionNotMet -> "Condicion no cumplida aun"
        }

        val updatedCosmetics = unlockablesManager.getPlayerCosmetics()
        val updatedItems = buildShopItems(currentState.selectedCategory, updatedCosmetics)

        updateState {
            it.copy(
                playerCosmetics = updatedCosmetics,
                items = updatedItems,
                purchaseMessage = message
            )
        }
    }

    private suspend fun equipItem(shopItem: ShopItem) {
        if (!shopItem.isOwned) return

        val category = shopItem.unlockable.category
        val currentEquipped = getEquippedId(category, currentState.playerCosmetics)

        val newEquipId = if (currentEquipped == shopItem.unlockable.id) {
            getDefaultIdForCategory(category)
        } else {
            shopItem.unlockable.id
        }

        unlockablesManager.equipItem(newEquipId, category)

        val updatedCosmetics = unlockablesManager.getPlayerCosmetics()
        val updatedItems = buildShopItems(category, updatedCosmetics)

        updateState {
            it.copy(
                playerCosmetics = updatedCosmetics,
                items = updatedItems
            )
        }
    }

    private fun buildShopItems(
        category: CosmeticCategory,
        playerCosmetics: PlayerCosmetics
    ): List<ShopItem> {
        return unlockablesManager.getCatalogByCategory(category).map { unlockable ->
            val isOwned = unlockable.id in playerCosmetics.unlockedIds
            val equippedId = getEquippedId(category, playerCosmetics)
            ShopItem(
                unlockable = unlockable,
                isOwned = isOwned,
                isEquipped = unlockable.id == equippedId
            )
        }
    }

    private fun getEquippedId(category: CosmeticCategory, cosmetics: PlayerCosmetics): String {
        return when (category) {
            CosmeticCategory.PROFILE_FRAME -> cosmetics.equippedFrame
            CosmeticCategory.TITLE_BADGE -> cosmetics.equippedTitle
            CosmeticCategory.ANSWER_CARD_THEME -> cosmetics.equippedCardTheme
            CosmeticCategory.CELEBRATION_ANIMATION -> cosmetics.equippedCelebration
            CosmeticCategory.APP_ICON -> cosmetics.equippedAppIcon
        }
    }

    private fun getDefaultIdForCategory(category: CosmeticCategory): String {
        return when (category) {
            CosmeticCategory.PROFILE_FRAME -> "frame_default"
            CosmeticCategory.TITLE_BADGE -> "title_default"
            CosmeticCategory.ANSWER_CARD_THEME -> "card_default"
            CosmeticCategory.CELEBRATION_ANIMATION -> "celebration_default"
            CosmeticCategory.APP_ICON -> ""
        }
    }
}
