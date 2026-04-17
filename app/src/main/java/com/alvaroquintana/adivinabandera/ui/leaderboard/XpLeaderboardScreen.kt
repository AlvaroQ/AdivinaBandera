package com.alvaroquintana.adivinabandera.ui.leaderboard

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.managers.Analytics
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameBronze
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameSilver
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.domain.XpLeaderboardEntry

@Composable
fun XpLeaderboardScreen(
    viewModel: XpLeaderboardViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        Analytics.analyticsScreenViewed(Analytics.SCREEN_XP_LEADERBOARD)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
    ) {
        if (uiState.isLoading) {
            LoadingState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                item {
                    XpLeaderboardHeaderCard()
                }

                if (uiState.entries.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Nadie en el ranking aun. jSe el primero!",
                                fontFamily = DynaPuffFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(uiState.entries) { index, entry ->
                        XpLeaderboardItem(
                            position = index + 1,
                            entry = entry
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun XpLeaderboardHeaderCard() {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = GameGold,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Ranking de XP",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Top 100 jugadores por experiencia",
                    fontFamily = DynaPuffFamily,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun XpLeaderboardItem(
    position: Int,
    entry: XpLeaderboardEntry
) {
    val podiumColor = when (position) {
        1    -> GameGold
        2    -> GameSilver
        3    -> GameBronze
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val surfaceColor = when (position) {
        1    -> GameGold.copy(alpha = 0.08f)
        2    -> GameSilver.copy(alpha = 0.08f)
        3    -> GameBronze.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = surfaceColor,
        shadowElevation = if (position <= 3) 2.dp else 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Posicion
            Text(
                text = "#$position",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = podiumColor,
                modifier = Modifier.width(40.dp)
            )

            // Avatar placeholder
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            // Nombre y titulo
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.nickname.ifBlank { "Anonimo" },
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = PillShape,
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "Niv. ${entry.level} · ${entry.title}",
                        fontFamily = DynaPuffCondensedFamily,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // XP total
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatXp(entry.totalXp),
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "XP",
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatXp(xp: Int): String = when {
    xp >= 1_000_000 -> "${xp / 1_000_000}M"
    xp >= 1_000     -> "${xp / 1_000}k"
    else             -> xp.toString()
}
