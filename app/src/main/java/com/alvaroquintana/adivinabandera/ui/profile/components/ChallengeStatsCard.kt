package com.alvaroquintana.adivinabandera.ui.profile.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.ElevationTokens
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.domain.challenge.ChallengeStats

@Composable
internal fun ChallengeStatsCard(
    stats: ChallengeStats,
    modifier: Modifier = Modifier
) {
    val accentColor = if (isAppInDarkTheme()) MaterialTheme.colorScheme.primary else GeoNavySoftLight

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level1)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "🏆", fontSize = 18.sp)
                Text(
                    text = "Desafíos Diarios",
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                AssistChip(
                    onClick = {},
                    label = {
                        Text(
                            text = "${stats.totalCompleted} completados",
                            fontFamily = DynaPuffSemiCondensedFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                    },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = accentColor.copy(alpha = 0.16f),
                        labelColor = accentColor
                    )
                )
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatItem(
                    label = "Días perfectos",
                    value = stats.totalAllDailyCompleteDays.toString(),
                    emoji = "⭐",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Racha actual",
                    value = "${stats.currentAllDailyStreak} días",
                    emoji = "🔥",
                    modifier = Modifier.weight(1f)
                )
                StatItem(
                    label = "Mejor racha",
                    value = "${stats.bestAllDailyStreak} días",
                    emoji = "🌟",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
