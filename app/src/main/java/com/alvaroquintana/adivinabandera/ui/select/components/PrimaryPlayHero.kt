package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Help
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoft
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme

@Composable
fun PrimaryPlayHero(
    streakLabel: String,
    bestStreakLabel: String,
    streakCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()

    val heroBrush = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0E1B38), Color(0xFF1A2A4A)))
    } else {
        Brush.verticalGradient(listOf(GeoNavySoft, GeoNavySoftLight))
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 120.dp)
            .shadow(if (isDark) 0.dp else 16.dp, RoundedCornerShape(24.dp), spotColor = Color(0x401A3B6D))
            .then(
                if (isDark) Modifier.border(1.dp, DarkGeoBorder, RoundedCornerShape(24.dp))
                else Modifier
            ),
        shape = RoundedCornerShape(24.dp),
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(heroBrush)
                .padding(horizontal = 20.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // CTA row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.play).uppercase(),
                        fontFamily = DynaPuffFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Icon(
                    imageVector = Icons.Rounded.Help,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
            }

            // Streak info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = streakLabel,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Text(
                    text = bestStreakLabel,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.55f)
                )
            }
        }
    }
}
