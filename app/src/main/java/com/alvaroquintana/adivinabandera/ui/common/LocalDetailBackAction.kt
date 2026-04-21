package com.alvaroquintana.adivinabandera.ui.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalDetailBackAction = compositionLocalOf<MutableState<(() -> Unit)?>> {
    error("LocalDetailBackAction not provided")
}
