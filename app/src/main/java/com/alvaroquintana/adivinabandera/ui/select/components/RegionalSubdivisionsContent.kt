package com.alvaroquintana.adivinabandera.ui.select.components

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.common.LocalAnimatedContentScope
import com.alvaroquintana.adivinabandera.ui.common.LocalSharedTransitionScope
import com.alvaroquintana.adivinabandera.ui.theme.DarkGeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.DarkSurface
import com.alvaroquintana.adivinabandera.ui.theme.GeoAmberLight
import com.alvaroquintana.adivinabandera.ui.theme.GeoBorder
import com.alvaroquintana.adivinabandera.ui.theme.GeoTextMuted
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.domain.GameMode
import com.alvaroquintana.domain.RegionalModeDescriptor

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun RegionalSubdivisionsContent(
    descriptors: List<RegionalModeDescriptor>,
    onNavigateToRegion: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) DarkSurface else Color.White
    val borderColor = if (isDark) DarkGeoBorder else GeoBorder
    val textPrimary = MaterialTheme.colorScheme.onBackground
    val textMuted = if (isDark) Color(0xFF636E80) else GeoTextMuted
    val chevronColor = if (isDark) DarkGeoBorder else Color(0xFFC0C8D4)

    val theme = CardTheme(cardBg, borderColor, textPrimary, textMuted, chevronColor, isDark)

    val sharedScope = LocalSharedTransitionScope.current
    val animScope = LocalAnimatedContentScope.current

    val animatedBodyModifier = if (sharedScope != null && animScope != null) {
        with(animScope) {
            Modifier.animateEnterExit(
                enter = slideInVertically(
                    animationSpec = tween(400, delayMillis = 50, easing = FastOutSlowInEasing),
                    initialOffsetY = { it }
                ) + fadeIn(tween(300, delayMillis = 100))
            )
        }
    } else {
        Modifier
    }

    val sharedBoundsModifier = if (sharedScope != null && animScope != null) {
        with(sharedScope) {
            Modifier.sharedBounds(
                sharedContentState = rememberSharedContentState(key = "select-regional-bounds"),
                animatedVisibilityScope = animScope
            )
        }
    } else {
        Modifier
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .then(sharedBoundsModifier)
    ) {
        Column(
            modifier = Modifier
                .clipToBounds()
                .then(animatedBodyModifier)
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            Text(
                text = stringResource(R.string.regional_subdivisions_header),
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = textPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 14.dp)
            )

            descriptors.forEachIndexed { index, descriptor ->
                RegionalCardForDescriptor(
                    descriptor = descriptor,
                    theme = theme,
                    onClick = { onNavigateToRegion(descriptor.mode) }
                )
                if (index < descriptors.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun RegionalCardForDescriptor(
    descriptor: RegionalModeDescriptor,
    theme: CardTheme,
    onClick: () -> Unit
) {
    val flag = flagEmojiFor(descriptor.alpha2)
    val title = stringResource(regionTitleRes(descriptor.alpha2))
    val isCompleted = descriptor.correctAnswersInMode >= descriptor.requiredToUnlockNext

    val subtitle = when {
        !descriptor.isUnlocked -> stringResource(
            R.string.region_locked_subtitle,
            stringResource(regionTitleRes(prevAlpha2ForRegion(descriptor.alpha2)))
        )
        isCompleted -> stringResource(R.string.region_completed_subtitle)
        else -> stringResource(R.string.region_active_subtitle)
    }

    val progressLabel = when {
        !descriptor.isUnlocked -> stringResource(
            R.string.region_progress_prereq,
            descriptor.prerequisiteCorrectAnswers,
            descriptor.requiredToUnlockNext
        )
        !isCompleted -> stringResource(
            R.string.region_progress_self,
            descriptor.correctAnswersInMode,
            descriptor.requiredToUnlockNext
        )
        else -> null
    }

    val progress = if (!descriptor.isUnlocked) descriptor.unlockSelfProgress
                   else descriptor.unlockNextProgress

    RegionalModeCard(
        flagEmoji = flag,
        title = title,
        subtitle = subtitle,
        theme = theme,
        onClick = onClick,
        isLocked = !descriptor.isUnlocked,
        isNearUnlock = !descriptor.isUnlocked && descriptor.unlockSelfProgress >= 0.75f,
        progress = progress,
        progressLabel = progressLabel,
        accentColor = GeoAmberLight,
        isCompleted = isCompleted
    )
}

private fun flagEmojiFor(alpha2: String): String = when (alpha2) {
    "ES" -> "\uD83C\uDDEA\uD83C\uDDF8"
    "MX" -> "\uD83C\uDDF2\uD83C\uDDFD"
    "AR" -> "\uD83C\uDDE6\uD83C\uDDF7"
    "BR" -> "\uD83C\uDDE7\uD83C\uDDF7"
    "DE" -> "\uD83C\uDDE9\uD83C\uDDEA"
    "US" -> "\uD83C\uDDFA\uD83C\uDDF8"
    else -> ""
}

private fun regionTitleRes(alpha2: String): Int = when (alpha2) {
    "ES" -> R.string.region_spain_title
    "MX" -> R.string.region_mexico_title
    "AR" -> R.string.region_argentina_title
    "BR" -> R.string.region_brazil_title
    "DE" -> R.string.region_germany_title
    "US" -> R.string.region_usa_title
    else -> R.string.region_spain_title
}

private fun prevAlpha2ForRegion(alpha2: String): String = when (alpha2) {
    "MX" -> "ES"
    "AR" -> "MX"
    "BR" -> "AR"
    "DE" -> "BR"
    "US" -> "DE"
    else -> "ES"
}
