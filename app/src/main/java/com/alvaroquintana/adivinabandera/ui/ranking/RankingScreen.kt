package com.alvaroquintana.adivinabandera.ui.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alvaroquintana.adivinabandera.ui.components.LeaderboardHeaderCard
import com.alvaroquintana.adivinabandera.ui.components.LeaderboardItem
import com.alvaroquintana.adivinabandera.ui.components.LoadingState
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.getBackgroundGradient
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme

@Composable
private fun rankingAccentColor(): Color {
    return if (isAppInDarkTheme()) MaterialTheme.colorScheme.primary else GeoNavySoftLight
}

@Composable
fun RankingScreen(
    viewModel: RankingViewModel
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    val isLoading = uiState.isLoading
    val rankingList = uiState.entries

    LaunchedEffect(Unit) {
        viewModel.dispatch(RankingViewModel.Intent.Load)
    }

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
                    LeaderboardHeaderCard(
                        title = "World Ranking",
                        subtitle = "Top 20 global scores",
                        titleColor = rankingAccentColor(),
                        subtitleColor = rankingAccentColor().copy(alpha = 0.75f)
                    )
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
                        LeaderboardItem(
                            position = index + 1,
                            name = user.name.ifBlank { "Anonymous" },
                            scoreText = user.score.toString(),
                            scoreColor = rankingAccentColor()
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

