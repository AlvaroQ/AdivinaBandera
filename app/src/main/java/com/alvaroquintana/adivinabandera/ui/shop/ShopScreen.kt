package com.alvaroquintana.adivinabandera.ui.shop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.ui.components.CurrencyDisplay
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameGreen
import com.alvaroquintana.adivinabandera.ui.theme.GameBlue
import com.alvaroquintana.adivinabandera.ui.theme.GameAnswerRed
import com.alvaroquintana.adivinabandera.ui.theme.GameMuted
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.domain.cosmetics.CosmeticCategory
import com.alvaroquintana.domain.cosmetics.CosmeticTier
import com.alvaroquintana.domain.cosmetics.UnlockCondition
import org.koin.androidx.compose.koinViewModel

@Composable
fun ShopScreen(
    onBack: () -> Unit,
    viewModel: ShopViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Mostrar mensaje de compra como snackbar
    LaunchedEffect(uiState.purchaseMessage) {
        uiState.purchaseMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.onPurchaseMessageDismissed()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
    ) {
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header con balance y boton volver
                ShopHeader(
                    balance = uiState.balance,
                    onBack = onBack
                )

                // Pestanas de categorias
                CategoryTabs(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onCategorySelected(it) }
                )

                // Grilla de items
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = uiState.items,
                        key = { it.unlockable.id }
                    ) { shopItem ->
                        ShopItemCard(
                            shopItem = shopItem,
                            onTap = { viewModel.onItemTapped(shopItem) }
                        )
                    }
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    // Dialogo de confirmacion de compra
    if (uiState.showPurchaseConfirmDialog) {
        uiState.pendingPurchaseItem?.let { item ->
            PurchaseConfirmDialog(
                shopItem = item,
                balance = uiState.balance,
                onConfirm = { viewModel.onPurchaseConfirmed() },
                onDismiss = { viewModel.onPurchaseDismissed() }
            )
        }
    }
}

@Composable
private fun ShopHeader(
    balance: com.alvaroquintana.domain.cosmetics.CurrencyBalance,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextButton(onClick = onBack) {
            Text(
                text = "< Volver",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Tienda",
            fontFamily = DynaPuffFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.onBackground
        )

        CurrencyDisplay(
            balance = balance,
            modifier = Modifier.padding(end = 4.dp)
        )
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: CosmeticCategory,
    onCategorySelected: (CosmeticCategory) -> Unit
) {
    val categories = listOf(
        CosmeticCategory.PROFILE_FRAME to "Marcos",
        CosmeticCategory.TITLE_BADGE to "Titulos",
        CosmeticCategory.ANSWER_CARD_THEME to "Tarjetas",
        CosmeticCategory.CELEBRATION_ANIMATION to "Celebraciones",
        CosmeticCategory.APP_ICON to "Icono"
    )

    val selectedIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0)

    PrimaryScrollableTabRow(
        selectedTabIndex = selectedIndex,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 8.dp,
        divider = {}
    ) {
        categories.forEachIndexed { index, (category, label) ->
            val isSelected = index == selectedIndex
            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
            ) {
                Surface(
                    shape = PillShape,
                    color = if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = label,
                        fontFamily = DynaPuffFamily,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 13.sp,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ShopItemCard(
    shopItem: ShopItem,
    onTap: () -> Unit
) {
    val tierColor = tierColor(shopItem.unlockable.tier)
    val isInteractable = shopItem.isOwned ||
        shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithCoins ||
        shopItem.unlockable.unlockCondition is UnlockCondition.PurchaseWithGems

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (shopItem.isEquipped) 12.dp else 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = tierColor
            )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .clickable(enabled = isInteractable) { onTap() },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = if (shopItem.isEquipped) 2.dp else 1.dp,
                        brush = if (shopItem.isEquipped) Brush.linearGradient(
                            listOf(tierColor, tierColor.copy(alpha = 0.6f))
                        ) else Brush.linearGradient(
                            listOf(tierColor.copy(alpha = 0.3f), tierColor.copy(alpha = 0.1f))
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                    .padding(12.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Icono emoji de la categoria
                    Text(
                        text = categoryEmoji(shopItem.unlockable.category),
                        style = MaterialTheme.typography.displaySmall
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Nombre del item
                    Text(
                        text = shopItem.unlockable.name,
                        fontFamily = DynaPuffFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Badge de tier
                    TierBadge(tier = shopItem.unlockable.tier, color = tierColor)

                    Spacer(modifier = Modifier.height(8.dp))

                    // Precio o condicion
                    PriceOrCondition(
                        unlockCondition = shopItem.unlockable.unlockCondition,
                        isOwned = shopItem.isOwned
                    )

                    // Indicador de equipado
                    if (shopItem.isEquipped) {
                        Spacer(modifier = Modifier.height(6.dp))
                        EquippedBadge()
                    }
                }

                // Badge "TUYO" arriba a la derecha si es propio (pero no equipado)
                if (shopItem.isOwned && !shopItem.isEquipped) {
                    OwnedBadge(
                        modifier = Modifier.align(Alignment.TopEnd)
                    )
                }
            }
        }
    }
}

@Composable
private fun TierBadge(tier: CosmeticTier, color: Color) {
    val label = when (tier) {
        CosmeticTier.COMMON -> "Comun"
        CosmeticTier.RARE -> "Raro"
        CosmeticTier.EPIC -> "Epico"
        CosmeticTier.LEGENDARY -> "Legendario"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.18f))
            .border(
                width = 1.dp,
                color = color.copy(alpha = 0.5f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontFamily = DynaPuffSemiCondensedFamily,
            fontSize = 11.sp,
            color = color
        )
    }
}

@Composable
private fun PriceOrCondition(
    unlockCondition: UnlockCondition,
    isOwned: Boolean
) {
    if (isOwned) {
        Text(
            text = "Desbloqueado",
            fontFamily = DynaPuffSemiCondensedFamily,
            fontSize = 12.sp,
            color = GameGreen
        )
        return
    }

    when (unlockCondition) {
        is UnlockCondition.PurchaseWithCoins -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "\uD83E\uDE99",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unlockCondition.price.toString(),
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = GameGold
                )
            }
        }
        is UnlockCondition.PurchaseWithGems -> {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "\uD83D\uDC8E",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unlockCondition.price.toString(),
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
        is UnlockCondition.ReachLevel -> {
            Text(
                text = "Nivel ${unlockCondition.level}",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = GameGold
            )
        }
        is UnlockCondition.StreakMilestone -> {
            Text(
                text = "${unlockCondition.days} dias de racha",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = GameAnswerRed
            )
        }
        is UnlockCondition.ChallengeCount -> {
            Text(
                text = "${unlockCondition.count} desafios",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        is UnlockCondition.CompleteAchievement -> {
            Text(
                text = "Via logro",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
        is UnlockCondition.Free -> {
            Text(
                text = "Gratis",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = GameGreen
            )
        }
    }
}

@Composable
private fun EquippedBadge() {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primary
    ) {
        Text(
            text = "Equipado",
            fontFamily = DynaPuffFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun OwnedBadge(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(bottomStart = 8.dp))
            .background(GameGreen.copy(alpha = 0.85f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "TUYO",
            fontFamily = DynaPuffFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}

@Composable
private fun PurchaseConfirmDialog(
    shopItem: ShopItem,
    balance: com.alvaroquintana.domain.cosmetics.CurrencyBalance,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val (priceText, canAfford) = when (val cond = shopItem.unlockable.unlockCondition) {
        is UnlockCondition.PurchaseWithCoins -> {
            "\uD83E\uDE99 ${cond.price} monedas" to (balance.coins >= cond.price)
        }
        is UnlockCondition.PurchaseWithGems -> {
            "\uD83D\uDC8E ${cond.price} gemas" to (balance.gems >= cond.price)
        }
        else -> "" to false
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Comprar ${shopItem.unlockable.name}",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column {
                Text(
                    text = shopItem.unlockable.description,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Precio: $priceText",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) GameGreen else GameAnswerRed
                )
                if (!canAfford) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Saldo insuficiente",
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontSize = 12.sp,
                        color = GameAnswerRed
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = canAfford
            ) {
                Text(
                    text = "Comprar",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    color = if (canAfford) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Cancelar",
                    fontFamily = DynaPuffFamily,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    )
}

// ==========================================
// Funciones auxiliares
// ==========================================

private fun tierColor(tier: CosmeticTier): Color = when (tier) {
    CosmeticTier.COMMON -> GameMuted             // gris azulado
    CosmeticTier.RARE -> GameBlue                // azul patriotico
    CosmeticTier.EPIC -> Color(0xFF7C3AED)       // purpura epico
    CosmeticTier.LEGENDARY -> GameGold           // dorado
}

private fun categoryEmoji(category: CosmeticCategory): String = when (category) {
    CosmeticCategory.PROFILE_FRAME -> "\uD83D\uDDBC\uFE0F"          // cuadro
    CosmeticCategory.TITLE_BADGE -> "\uD83C\uDFF7\uFE0F"            // etiqueta
    CosmeticCategory.ANSWER_CARD_THEME -> "\uD83C\uDFA8"            // paleta
    CosmeticCategory.CELEBRATION_ANIMATION -> "\uD83C\uDF89"        // fiesta
    CosmeticCategory.APP_ICON -> "\uD83D\uDCF1"                     // celular
}
