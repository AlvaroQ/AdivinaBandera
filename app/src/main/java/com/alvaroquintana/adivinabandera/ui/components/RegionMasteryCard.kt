package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBlue
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurface
import com.alvaroquintana.adivinabandera.ui.theme.GeoForest
import com.alvaroquintana.adivinabandera.ui.theme.GeoForestLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavy
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextMuted
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextSecondary
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme

private const val TOTAL_COUNTRIES = 250

/**
 * Tarjeta compacta que muestra el progreso de descubrimiento de paises.
 *
 * Muestra cuantos paises el jugador ha respondido correctamente al menos una vez
 * sobre el total de 250. Tappable — navega al InfoScreen (enciclopedia).
 *
 * [discoveredCount] puede ser 0 hasta que el jugador empiece a jugar.
 * [onTap] navega al InfoScreen.
 */
@Composable
fun RegionMasteryCard(
    discoveredCount: Int,
    onTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderColor = if (isDark) DarkGeoBorder else GeoBorder
    val textPrimary = if (isDark) Color(0xFFD0DAEA) else Color(0xFF1A1A2E)
    val textMuted = if (isDark) Color(0xFF636E80) else GeoTextMuted
    val accentColor = if (isDark) DarkGeoBlue else GeoNavy
    val progressColor = if (isDark) GeoForestLight else GeoForest
    val chevronColor = if (isDark) DarkGeoBorder else Color(0xFFC0C8D4)

    val progress = (discoveredCount.toFloat() / TOTAL_COUNTRIES).coerceIn(0f, 1f)
    val percentText = (progress * 100).toInt()

    Surface(
        onClick = onTap,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = cardBg,
        shadowElevation = if (isDark) 0.dp else 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono explorar
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            listOf(progressColor, progressColor.copy(alpha = 0.7f))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Explore,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.countries_discovered),
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = textPrimary
                    )
                    Text(
                        text = "$discoveredCount/$TOTAL_COUNTRIES",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = accentColor
                    )
                }

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.15f),
                    strokeCap = StrokeCap.Round
                )

                Text(
                    text = if (discoveredCount == 0) stringResource(R.string.play_to_discover)
                    else stringResource(R.string.explored_progress, percentText),
                    fontSize = 11.sp,
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
