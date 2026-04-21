package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass

@Composable
fun OptionGrid(
    options: List<String>,
    modifier: Modifier = Modifier,
    optionContent: @Composable (index: Int, text: String, modifier: Modifier) -> Unit
) {
    val windowSizeClass = LocalWindowSizeClass.current
    val useGrid = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact

    if (useGrid && options.size >= 4) {
        val indexed = options.mapIndexed { i, text -> i to text }
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            indexed.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { (index, option) ->
                        optionContent(index, option, Modifier.weight(1f))
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    } else {
        Column(
            modifier = modifier,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            options.forEachIndexed { index, option ->
                optionContent(index, option, Modifier.fillMaxWidth())
            }
        }
    }
}
