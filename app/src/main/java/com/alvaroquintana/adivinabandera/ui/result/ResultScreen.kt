package com.alvaroquintana.adivinabandera.ui.result

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.components.ChallengeCompletionBanner
import com.alvaroquintana.adivinabandera.ui.components.ConfettiOverlay
import com.alvaroquintana.adivinabandera.ui.components.CurrencyDisplay
import com.alvaroquintana.adivinabandera.ui.components.ModeUnlockCelebration
import com.alvaroquintana.adivinabandera.ui.components.MysteryBoxDialog
import com.alvaroquintana.adivinabandera.ui.components.ShimmerBox
import com.alvaroquintana.adivinabandera.ui.components.StreakCelebrationDialog
import com.alvaroquintana.domain.cosmetics.CurrencyBalance
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.domain.Achievement
import com.alvaroquintana.domain.User
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    gamePoints: Int,
    onPlayAgain: () -> Unit,
    onShare: () -> Unit,
    onRate: () -> Unit,
    onViewRanking: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingPoints by remember { mutableStateOf("") }

    // Observe navigation events to show the save dialog
    LaunchedEffect(Unit) {
        viewModel.navigation.collect { nav ->
            when (nav) {
                is ResultViewModel.Navigation.Dialog -> {
                    pendingPoints = nav.points
                    showSaveDialog = true
                }
                else -> { /* handled by parent route */ }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Stats row: puntos actuales, récord personal, récord mundial (con shimmer)
            item {
                StatsRow(
                    gamePoints = gamePoints,
                    uiState = uiState
                )
            }

            // Badge motivacional de engagement
            item {
                EngagementBadge(
                    uiState = uiState,
                    gamePoints = gamePoints
                )
            }

            // Badge de XP ganado
            item {
                XpGainedBadge(uiState = uiState)
            }

            // Badge de moneda virtual ganada
            if (uiState.coinsEarned > 0 || uiState.gemsEarned > 0) {
                item {
                    CurrencyEarnedBadge(
                        coinsEarned = uiState.coinsEarned,
                        gemsEarned = uiState.gemsEarned
                    )
                }
            }

            // Lista de logros desbloqueados
            if (uiState.newAchievements.isNotEmpty()) {
                item {
                    AchievementsSection(achievements = uiState.newAchievements)
                }
            }

            // Banner de desafios completados
            val challengeResult = uiState.challengeCompletionResult
            if (challengeResult != null && (challengeResult.completedChallenges.isNotEmpty() || challengeResult.allDailyJustCompleted)) {
                item {
                    ChallengeCompletionBanner(
                        completionResult = challengeResult,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Play again (primary)
            item {
                PlayAgainButton(onClick = onPlayAgain)
            }

            // View Ranking
            item {
                RankingButton(onClick = onViewRanking)
            }

            // Share
            item {
                ShareButton(onClick = onShare)
            }

            // Rate
            item {
                RateButton(onClick = onRate)
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        if (gamePoints > 0) {
            ConfettiOverlay()
        }
    }

    if (showSaveDialog) {
        SaveRankingDialog(
            points = pendingPoints,
            onSave = { name ->
                viewModel.saveTopScore(User(name = name, points = pendingPoints, score = pendingPoints.toIntOrNull() ?: 0))
                showSaveDialog = false
            },
            onDismiss = { showSaveDialog = false }
        )
    }

    if (uiState.showLevelUpDialog) {
        LevelUpDialog(
            newLevel = uiState.newLevel,
            newTitle = uiState.newTitle,
            xpGained = uiState.xpGained,
            onDismiss = { viewModel.dismissLevelUpDialog() }
        )
    }

    val streakResult = uiState.streakCheckResult
    if (uiState.showStreakDialog && streakResult != null) {
        StreakCelebrationDialog(
            streakCheckResult = streakResult,
            onDismiss = { viewModel.dismissStreakDialog() }
        )
    }

    val unlockEvent = uiState.unlockEvent
    if (unlockEvent is UnlockEvent.ModeUnlocked) {
        ModeUnlockCelebration(
            modeName = unlockEvent.modeName,
            onDismiss = { viewModel.dismissModeUnlockCelebration() }
        )
    }

    val mysteryBox = uiState.mysteryBoxReward
    if (mysteryBox != null) {
        MysteryBoxDialog(
            reward = mysteryBox,
            onDismiss = { viewModel.dismissMysteryBox() }
        )
    }
}

@Composable
private fun StatsRow(
    gamePoints: Int,
    uiState: ResultUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Columna 1: puntos de la partida (siempre visible)
        StatItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = "+$gamePoints",
            label = stringResource(R.string.result, gamePoints)
        )

        // Columna 2: récord personal (siempre visible)
        StatItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = uiState.personalRecord.toString(),
            label = stringResource(R.string.personal_record, "")
        )

        // Columna 3: récord mundial — siempre ocupa espacio, shimmer mientras carga
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState.worldRecord,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "world_record_content"
            ) { worldRecord ->
                if (worldRecord == null) {
                    // Shimmer mientras carga — mismas dimensiones que el StatItem
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ShimmerBox(modifier = Modifier.size(24.dp))
                        ShimmerBox(modifier = Modifier.height(18.dp).fillMaxWidth(0.7f))
                        ShimmerBox(modifier = Modifier.height(12.dp).fillMaxWidth(0.9f))
                    }
                } else {
                    StatItem(
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = worldRecord,
                        label = stringResource(R.string.world_record, "")
                    )
                }
            }
        }
    }
}

/**
 * Badge motivacional que aparece suavemente 600ms después del render inicial.
 * No muestra nada si es KEEP_TRYING sin diferencia de puntos (primera partida).
 */
@Composable
private fun EngagementBadge(
    uiState: ResultUiState,
    gamePoints: Int
) {
    // No mostrar si gamePoints = 0 o si no hay nada motivacional que decir
    if (gamePoints == 0) return

    var showBadge by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(600L)
        showBadge = true
    }

    AnimatedVisibility(
        visible = showBadge,
        enter = scaleIn() + fadeIn(animationSpec = tween(400))
    ) {
        val text = when (uiState.engagementLevel) {
            EngagementLevel.NEW_WORLD_RECORD ->
                stringResource(R.string.result_engagement_world_record, uiState.pointsDifference)
            EngagementLevel.NEW_PERSONAL_BEST ->
                stringResource(R.string.result_engagement_personal_best, uiState.pointsDifference)
            EngagementLevel.SO_CLOSE ->
                stringResource(R.string.result_engagement_so_close, uiState.pointsDifference)
            EngagementLevel.KEEP_TRYING ->
                stringResource(R.string.result_engagement_keep_trying)
        }

        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    value: String,
    label: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        icon()
        Text(
            text = value,
            fontFamily = DynaPuffSemiCondensedFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = label.trimEnd(':').trim(),
            fontFamily = DynaPuffSemiCondensedFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun PlayAgainButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = PillShape,
        color = MaterialTheme.colorScheme.primary
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.play_again),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun RankingButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = PillShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.best_scores),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun ShareButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = PillShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_share),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.share),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun RateButton(onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp),
        shape = PillShape,
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Star,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.rate_on_play_store),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/**
 * Badge animado que muestra el XP ganado y el desglose al final de la partida.
 * Aparece 900ms despues del render inicial para no competir con el badge de engagement.
 */
@Composable
private fun XpGainedBadge(uiState: ResultUiState) {
    if (uiState.xpGained <= 0) return

    var showXp by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.xpGained) {
        delay(900L)
        showXp = true
    }

    AnimatedVisibility(
        visible = showXp,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400))
    ) {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "+${uiState.xpGained} XP",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
                val breakdown = uiState.xpBreakdown
                if (breakdown != null) {
                    Text(
                        text = buildString {
                            append("Base: +${breakdown.base}")
                            if (breakdown.streakBonus > 0) append(" | Racha: +${breakdown.streakBonus}")
                            if (breakdown.perfectBonus > 0) append(" | Perfecto: +${breakdown.perfectBonus}")
                            if (breakdown.winBonus > 0) append(" | Bonus: +${breakdown.winBonus}")
                        },
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

/**
 * Badge que muestra monedas y/o gemas ganadas en la partida.
 */
@Composable
private fun CurrencyEarnedBadge(coinsEarned: Int, gemsEarned: Int) {
    var showCurrency by remember { mutableStateOf(false) }

    LaunchedEffect(coinsEarned, gemsEarned) {
        delay(1100L)
        showCurrency = true
    }

    AnimatedVisibility(
        visible = showCurrency,
        enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(animationSpec = tween(400))
    ) {
        Surface(
            shape = PillShape,
            color = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (coinsEarned > 0) {
                    Text(
                        text = "+$coinsEarned \uD83E\uDE99",
                        fontFamily = DynaPuffFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                if (gemsEarned > 0) {
                    Text(
                        text = "+$gemsEarned \uD83D\uDC8E",
                        fontFamily = DynaPuffFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Seccion que lista los logros desbloqueados en esta partida.
 */
@Composable
private fun AchievementsSection(achievements: List<Achievement>) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Logros desbloqueados",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            achievements.forEach { achievement ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = achievement.icon, fontSize = 20.sp)
                    Column {
                        Text(
                            text = achievement.id.replace("_", " ").replaceFirstChar { it.uppercase() },
                            fontFamily = DynaPuffSemiCondensedFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "+${achievement.xpReward} XP",
                            fontFamily = DynaPuffSemiCondensedFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog celebratorio que aparece cuando el jugador sube de nivel.
 */
@Composable
private fun LevelUpDialog(
    newLevel: Int,
    newTitle: String,
    xpGained: Int,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Nivel $newLevel",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Subiste de nivel",
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                if (newTitle.isNotBlank()) {
                    Text(
                        text = newTitle,
                        fontFamily = DynaPuffSemiCondensedFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+$xpGained XP ganados",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.tertiary,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "Continuar",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    )
}

@Composable
private fun SaveRankingDialog(
    points: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.dialog_ranking_congratulation),
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.dialog_ranking_description),
                    fontFamily = DynaPuffFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = {
                        Text(
                            text = stringResource(R.string.dialog_name),
                            fontFamily = DynaPuffFamily
                        )
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { if (name.isNotBlank()) onSave(name) },
                enabled = name.isNotBlank()
            ) {
                Text(
                    text = stringResource(R.string.dialog_save),
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(R.string.back),
                    fontFamily = DynaPuffFamily,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    )
}
