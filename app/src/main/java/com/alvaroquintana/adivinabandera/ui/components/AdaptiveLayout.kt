package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass

@Composable
fun CenteredContent(
    maxWidth: Dp = 560.dp,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier.widthIn(max = maxWidth),
            content = content
        )
    }
}

@Composable
fun AdaptivePane(
    modifier: Modifier = Modifier,
    primaryWeight: Float = 0.5f,
    primary: @Composable () -> Unit,
    secondary: @Composable () -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current

    if (windowSizeClass.widthSizeClass == WindowWidthSizeClass.Expanded) {
        Row(modifier = modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(primaryWeight)) {
                primary()
            }
            Box(modifier = Modifier.weight(1f - primaryWeight)) {
                secondary()
            }
        }
    } else {
        Box(modifier = modifier.fillMaxSize()) {
            primary()
        }
    }
}
