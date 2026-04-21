package com.alvaroquintana.adivinabandera.ui.result.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.components.ShimmerBox
import com.alvaroquintana.adivinabandera.ui.result.ResultUiState

@Composable
internal fun StatsRow(
    gamePoints: Int,
    uiState: ResultUiState
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Column 1: points of the current game (always visible)
        StatItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = "+$gamePoints",
            label = stringResource(R.string.result, gamePoints)
        )

        // Column 2: personal record (always visible)
        StatItem(
            modifier = Modifier.weight(1f),
            icon = {
                Icon(
                    Icons.Rounded.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(24.dp)
                )
            },
            value = uiState.personalRecord.toString(),
            label = stringResource(R.string.personal_record, "")
        )

        // Column 3: world record — always occupies space, shimmer while loading
        Box(modifier = Modifier.weight(1f)) {
            AnimatedContent(
                targetState = uiState.worldRecord,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(150))
                },
                label = "world_record_content"
            ) { worldRecord ->
                if (worldRecord == null) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ShimmerBox(modifier = Modifier.size(24.dp))
                        ShimmerBox(modifier = Modifier.height(18.dp).fillMaxWidth(0.7f))
                        ShimmerBox(modifier = Modifier.height(12.dp).fillMaxWidth(0.9f))
                    }
                } else {
                    StatItem(
                        modifier = Modifier.fillMaxWidth(),
                        icon = {
                            Icon(
                                Icons.Rounded.Star,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        value = worldRecord,
                        label = stringResource(R.string.world_record, "")
                    )
                }
            }
        }
    }
}
