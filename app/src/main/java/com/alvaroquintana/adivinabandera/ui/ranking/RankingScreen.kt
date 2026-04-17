package com.alvaroquintana.adivinabandera.ui.ranking

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GameSilver
import com.alvaroquintana.adivinabandera.ui.theme.GameBronze
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.domain.User

@Composable
private fun rankingAccentColor(): Color {
    return if (isAppInDarkTheme()) MaterialTheme.colorScheme.primary else GeoNavySoftLight
}

@Composable
fun RankingScreen(
    viewModel: RankingViewModel
) {
    val progress by viewModel.progress.collectAsStateWithLifecycle()
    val rankingList by viewModel.rankingList.collectAsStateWithLifecycle()

    val isLoading = progress is RankingViewModel.UiModel.Loading &&
            (progress as RankingViewModel.UiModel.Loading).show

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(getBackgroundGradient())
    ) {
        if (isLoading && rankingList.isEmpty()) {
            LoadingState()
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Header
                item {
                    RankingHeaderCard()
                }

                if (rankingList.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No scores yet. Be the first!",
                                fontFamily = DynaPuffFamily,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    itemsIndexed(rankingList) { index, user ->
                        RankingItem(
                            position = index + 1,
                            user = user
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun RankingHeaderCard() {
    val accentColor = rankingAccentColor()

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
                    text = "World Ranking",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = accentColor
                )
                Text(
                    text = "Top 20 global scores",
                    fontFamily = DynaPuffFamily,
                    fontSize = 13.sp,
                    color = accentColor.copy(alpha = 0.75f)
                )
            }
        }
    }
}

@Composable
private fun RankingItem(
    position: Int,
    user: User
) {
    val accentColor = rankingAccentColor()

    val podiumColor = when (position) {
        1 -> GameGold
        2 -> GameSilver
        3 -> GameBronze
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    }

    val surfaceColor = when (position) {
        1 -> GameGold.copy(alpha = 0.08f)
        2 -> GameSilver.copy(alpha = 0.08f)
        3 -> GameBronze.copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.surface
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 1.dp,
        border = if (position <= 3) BorderStroke(2.dp, podiumColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Position badge
            Text(
                text = "#$position",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = podiumColor,
                modifier = Modifier.width(40.dp)
            )

            // User icon
            Icon(
                imageVector = Icons.Rounded.Person,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Name
            Text(
                text = user.name.ifBlank { "Anonymous" },
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            // Score
            Text(
                text = user.score.toString(),
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = accentColor
            )
        }
    }
}
