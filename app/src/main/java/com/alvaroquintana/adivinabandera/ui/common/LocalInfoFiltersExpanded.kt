package com.alvaroquintana.adivinabandera.ui.common

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.compositionLocalOf

val LocalInfoFiltersExpanded = compositionLocalOf<MutableState<Boolean>> {
    error("LocalInfoFiltersExpanded not provided")
}
