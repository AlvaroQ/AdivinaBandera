package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.scaleIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.animation.AnimationSpecs
import kotlinx.coroutines.delay
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.LocalGameColors
import com.alvaroquintana.adivinabandera.ui.theme.PillShape

enum class AnswerState { NEUTRAL, SELECTED, CORRECT, WRONG }

@Composable
fun AnswerOptionCard(
    text: String,
    state: AnswerState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val gameColors = LocalGameColors.current
    val haptic = LocalHapticFeedback.current

    val targetContainerColor: Color = when (state) {
        AnswerState.NEUTRAL -> MaterialTheme.colorScheme.surface
        AnswerState.SELECTED -> MaterialTheme.colorScheme.surface
        AnswerState.CORRECT -> gameColors.correctContainer
        AnswerState.WRONG -> gameColors.wrongContainer
    }

    val targetBorderColor: Color = when (state) {
        AnswerState.NEUTRAL -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
        AnswerState.SELECTED -> MaterialTheme.colorScheme.primary
        AnswerState.CORRECT -> gameColors.correctAnswer
        AnswerState.WRONG -> gameColors.wrongAnswer
    }

    val targetContentColor: Color = when (state) {
        AnswerState.NEUTRAL -> MaterialTheme.colorScheme.onSurface
        AnswerState.SELECTED -> MaterialTheme.colorScheme.onSurface
        AnswerState.CORRECT -> gameColors.onCorrectContainer
        AnswerState.WRONG -> gameColors.onWrongContainer
    }

    val animatedContainerColor by animateColorAsState(
        targetValue = targetContainerColor,
        animationSpec = AnimationSpecs.AnswerColorSpec,
        label = "containerColor"
    )

    val animatedBorderColor by animateColorAsState(
        targetValue = targetBorderColor,
        animationSpec = AnimationSpecs.AnswerColorSpec,
        label = "borderColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = targetContentColor,
        animationSpec = AnimationSpecs.AnswerColorSpec,
        label = "contentColor"
    )

    val targetScale = when (state) {
        AnswerState.SELECTED -> 1.02f
        AnswerState.CORRECT -> 1.0f
        else -> 1.0f
    }

    val animatedScale by animateFloatAsState(
        targetValue = targetScale,
        animationSpec = when (state) {
            AnswerState.CORRECT -> AnimationSpecs.CorrectBounceSpec
            else -> spring(stiffness = Spring.StiffnessMedium)
        },
        label = "scale"
    )

    val shakeOffset = remember { Animatable(0f) }

    LaunchedEffect(state) {
        if (state == AnswerState.WRONG) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = AnimationSpecs.wrongShakeSpec()
            )
        } else {
            shakeOffset.snapTo(0f)
        }
    }

    LaunchedEffect(state) {
        when (state) {
            AnswerState.SELECTED -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            AnswerState.CORRECT -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            AnswerState.WRONG -> {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                delay(100)
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            }
            else -> {}
        }
    }

    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .scale(animatedScale)
            .offset { IntOffset(shakeOffset.value.dp.roundToPx(), 0) },
        enabled = enabled,
        shape = PillShape,
        color = animatedContainerColor,
        border = BorderStroke(1.dp, animatedBorderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                fontFamily = DynaPuffSemiCondensedFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = animatedContentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

            AnimatedVisibility(
                visible = state == AnswerState.CORRECT,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = "Correct",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp),
                    tint = gameColors.correctAnswer
                )
            }

            AnimatedVisibility(
                visible = state == AnswerState.WRONG,
                enter = scaleIn(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Wrong",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(24.dp),
                    tint = gameColors.wrongAnswer
                )
            }
        }
    }
}
