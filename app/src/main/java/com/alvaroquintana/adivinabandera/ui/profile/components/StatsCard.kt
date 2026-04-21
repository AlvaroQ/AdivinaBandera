package com.alvaroquintana.adivinabandera.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.ElevationTokens

@Composable
internal fun StatsCard(
    totalGamesPlayed: Int,
    accuracy: Float,
    bestStreakEver: Int,
    totalPerfectGames: Int,
    totalTimePlayed: Long,
    modifier: Modifier = Modifier
) {
    val totalMinutes = totalTimePlayed / 60_000L
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    val timeText = if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    val accuracyText = "${(accuracy * 100).toInt()}%"

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadísticas",
                fontFamily = DynaPuffFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Partidas",
                    value = totalGamesPlayed.toString(),
                    emoji = "🎮",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Precisión",
                    value = accuracyText,
                    emoji = "🎯",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Mejor racha",
                    value = bestStreakEver.toString(),
                    emoji = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Perfectas",
                    value = totalPerfectGames.toString(),
                    emoji = "✨",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(10.dp))

            StatItem(
                label = "Tiempo jugado",
                value = timeText,
                emoji = "⏱️",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
