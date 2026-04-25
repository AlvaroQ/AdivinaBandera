package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.components.AnimatedStreakFlame
import com.alvaroquintana.adivinabandera.ui.components.DailyRewardCard
import com.alvaroquintana.adivinabandera.ui.components.RegionMasteryCard
import com.alvaroquintana.adivinabandera.ui.components.StreakAtRiskBanner
import com.alvaroquintana.adivinabandera.ui.select.SelectUiState
import com.alvaroquintana.adivinabandera.ui.select.SelectViewModel
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurface
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.GeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextMuted
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme

@Composable
fun SelectHomeContent(
    viewModel: SelectViewModel,
    uiState: SelectUiState,
    onNavigateToGame: () -> Unit,
    onModesClick: () -> Unit,
    onNavigateToLearn: () -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToShop: () -> Unit,
    onNavigateToPractice: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    val bgColor = MaterialTheme.colorScheme.background
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderColor = if (isDark) GeoBorder else GeoBorder
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = if (isDark) Color(0xFF636E80) else GeoTextMuted
    val chevronColor = if (isDark) Color(0xFF636E80) else Color(0xFFC0C8D4)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ── Hero card (streak + bono diario) ────────────────────────────────
            val heroBackground = if (isDark) bgColor else Color.White
            val heroTextPrimary = if (isDark) MaterialTheme.colorScheme.onBackground else Color(0xFF1A2A4A)
            val heroTextMuted = if (isDark) Color(0xFF636E80) else Color(0xFF6B7A8D)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(if (isDark) 0.dp else 12.dp, RoundedCornerShape(20.dp), spotColor = Color(0x301A3B6D))
                    .clip(RoundedCornerShape(20.dp))
                    .background(heroBackground)
                    .then(
                        if (isDark) Modifier
                        else Modifier.border(1.dp, GeoBorder, RoundedCornerShape(20.dp))
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedStreakFlame(currentStreak = uiState.streakState.currentStreak)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.streak_label, uiState.streakState.currentStreak),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = heroTextPrimary
                        )
                        Text(
                            text = stringResource(R.string.best_streak_label, uiState.streakState.bestStreak),
                            fontSize = 11.sp,
                            color = heroTextMuted
                        )
                    }
                }

                DailyRewardCard(
                    reward = uiState.dailyReward,
                    onClaim = { viewModel.dispatch(com.alvaroquintana.adivinabandera.ui.select.SelectViewModel.Intent.ClaimDailyReward) },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Primary Play Hero (Classic mode CTA) ───────────────────────────
            PrimaryPlayHero(
                streakLabel = stringResource(R.string.streak_label, uiState.streakState.currentStreak),
                bestStreakLabel = stringResource(R.string.best_streak_label, uiState.streakState.bestStreak),
                streakCount = uiState.streakState.currentStreak,
                onClick = onNavigateToGame,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Modes preview row (tap to expand all modes) ────────────────────
            ModesPreviewRow(
                modesDescriptors = uiState.gameModeDescriptors,
                onClick = onModesClick,
                modifier = Modifier.fillMaxWidth(),
                regionalUnlocked = uiState.unlockedRegionalCount > 0
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── Streak at risk banner ───────────────────────────────────────────
            if (uiState.isStreakAtRisk) {
                StreakAtRiskBanner(
                    currentStreak = uiState.streakState.currentStreak,
                    freezeTokens = uiState.streakState.freezeTokens,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            RegionMasteryCard(
                discoveredCount = uiState.discoveredCountries,
                onTap = onNavigateToLearn,
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.weakSpotCountryIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    onClick = onNavigateToPractice,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = cardBg,
                    shadowElevation = if (isDark) 0.dp else 4.dp,
                    border = BorderStroke(1.dp, borderColor)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(13.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFE53935), Color(0xFFEF9A9A))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SportsEsports,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.practice_weak_spots),
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = textPrimary
                            )
                            Text(
                                text = stringResource(
                                    R.string.weak_spot_countries,
                                    uiState.weakSpotCountryIds.size
                                ),
                                fontSize = 12.sp,
                                color = textMuted
                            )
                        }
                        Icon(
                            imageVector = Icons.Rounded.ChevronRight,
                            contentDescription = null,
                            tint = chevronColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
