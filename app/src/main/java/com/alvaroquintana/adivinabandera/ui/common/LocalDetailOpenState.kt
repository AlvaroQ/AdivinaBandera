package com.alvaroquintana.adivinabandera.ui.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalDetailOpenState = compositionLocalOf<MutableState<Boolean>> {
    error("LocalDetailOpenState not provided")
}
