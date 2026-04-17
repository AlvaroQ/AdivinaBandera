package com.alvaroquintana.adivinabandera.ui.composables

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameRed

@Composable
fun GameAppBar(
    title: String,
    onBackClick: () -> Unit,
    showLives: Boolean = true,
    lives: Int = 3
) {
    Surface(
        color = MaterialTheme.colorScheme.background,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }

            Text(
                text = title,
                color = MaterialTheme.colorScheme.onBackground,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f)
            )

            if (showLives) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    repeat(3) { index ->
                        AnimatedHeartIcon(index = index, isAlive = lives > index)
                    }
                }
            } else {
                Spacer(modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
private fun AnimatedHeartIcon(index: Int, isAlive: Boolean) {
    val scale by animateFloatAsState(
        targetValue = if (isAlive) 1f else 0.7f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "heart_scale"
    )

    val heartBeat: Float = if (isAlive) {
        val infiniteTransition = rememberInfiniteTransition(label = "heart_beat_$index")
        val beat by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.15f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = index * 100),
                repeatMode = RepeatMode.Reverse
            ),
            label = "heart_beat"
        )
        beat
    } else {
        1f
    }

    Icon(
        imageVector = if (isAlive) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
        contentDescription = null,
        tint = if (isAlive) GameRed else Color.White.copy(alpha = 0.3f),
        modifier = Modifier
            .size(24.dp)
            .scale(scale * heartBeat)
            .drawBehind {
                if (isAlive) {
                    drawCircle(
                        color = GameRed.copy(alpha = 0.2f),
                        radius = size.minDimension * 0.55f
                    )
                }
            }
    )
}
