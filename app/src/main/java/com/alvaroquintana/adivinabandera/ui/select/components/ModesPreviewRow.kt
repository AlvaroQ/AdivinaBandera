package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Paid
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurface
import com.alvaroquintana.adivinabandera.ui.theme.GeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmber
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmberLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoForest
import com.alvaroquintana.adivinabandera.ui.theme.GeoForestLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoGold
import com.alvaroquintana.adivinabandera.ui.theme.GeoGoldLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurple
import com.alvaroquintana.adivinabandera.ui.theme.GeoPurpleLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTeal
import com.alvaroquintana.adivinabandera.ui.theme.GeoTealLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextMuted
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.GameModeDescriptor

private data class MiniIconSpec(
    val mode: GameMode,
    val icon: ImageVector,
    val gradient: Brush
)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun ModesPreviewRow(
    modesDescriptors: List<GameModeDescriptor>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderColor = if (isDark) DarkGeoBorder else GeoBorder
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = if (isDark) Color(0xFF636E80) else GeoTextMuted
    val chevronColor = if (isDark) DarkGeoBorder else Color(0xFFC0C8D4)
    val lockedContainer = if (isDark) Color(0xFF2B3342) else Color(0xFFE5EAF2)
    val lockedIconTint = if (isDark) Color(0xFF8F9BB2) else Color(0xFF8A96A8)

    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedContentScope.current

    val descriptorByMode = modesDescriptors.associateBy { it.mode }
    val miniIcons = listOf(
        MiniIconSpec(GameMode.CapitalByFlag, Icons.Rounded.LocationCity, Brush.verticalGradient(listOf(GeoForest, GeoForestLight))),
        MiniIconSpec(GameMode.CapitalByCountry, Icons.Rounded.Map, Brush.verticalGradient(listOf(GeoAmber, GeoAmberLight))),
        MiniIconSpec(GameMode.CurrencyDetective, Icons.Rounded.Paid, Brush.verticalGradient(listOf(GeoPurple, GeoPurpleLight))),
        MiniIconSpec(GameMode.PopulationChallenge, Icons.Rounded.BarChart, Brush.verticalGradient(listOf(GeoTeal, GeoTealLight))),
        MiniIconSpec(GameMode.WorldMix, Icons.Rounded.AutoAwesome, Brush.verticalGradient(listOf(GeoGold, GeoGoldLight)))
    )
    val unlockedModesCount = miniIcons.count { spec ->
        descriptorByMode[spec.mode]?.isUnlocked ?: (spec.mode.unlockLevel == 0)
    }

    val sharedBoundsModifier: Modifier = if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "select-modes-bounds"),
                animatedVisibilityScope = animScope
            )
        }
    } else {
        Modifier
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .then(sharedBoundsModifier),
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
            // Labels
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = stringResource(R.string.more_modes),
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = textPrimary
                )
                Text(
                    text = stringResource(
                        R.string.more_modes_subtitle,
                        unlockedModesCount,
                        miniIcons.size
                    ),
                    fontSize = 12.sp,
                    color = textMuted
                )
            }

            // Mini mode icons preview
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                miniIcons.forEach { spec ->
                    val isUnlocked = descriptorByMode[spec.mode]?.isUnlocked ?: (spec.mode.unlockLevel == 0)
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .then(
                                if (isUnlocked) Modifier.background(spec.gradient)
                                else Modifier.background(lockedContainer)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = spec.icon,
                            contentDescription = null,
                            tint = if (isUnlocked) Color.White else lockedIconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
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
