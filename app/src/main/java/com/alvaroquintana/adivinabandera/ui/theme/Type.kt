package com.alvaroquintana.adivinabandera.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.R

val DynaPuffFamily = FontFamily(
    Font(R.font.dynapuff_regular, FontWeight.Light),
    Font(R.font.dynapuff_regular, FontWeight.Normal),
    Font(R.font.dynapuff_medium, FontWeight.Medium),
    Font(R.font.dynapuff_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_bold, FontWeight.Bold)
)

val DynaPuffSemiCondensedFamily = FontFamily(
    Font(R.font.dynapuff_semicondensed_regular, FontWeight.Light),
    Font(R.font.dynapuff_semicondensed_regular, FontWeight.Normal),
    Font(R.font.dynapuff_semicondensed_medium, FontWeight.Medium),
    Font(R.font.dynapuff_semicondensed_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_semicondensed_bold, FontWeight.Bold)
)

val DynaPuffCondensedFamily = FontFamily(
    Font(R.font.dynapuff_condensed_regular, FontWeight.Light),
    Font(R.font.dynapuff_condensed_regular, FontWeight.Normal),
    Font(R.font.dynapuff_condensed_medium, FontWeight.Medium),
    Font(R.font.dynapuff_condensed_semibold, FontWeight.SemiBold),
    Font(R.font.dynapuff_condensed_bold, FontWeight.Bold)
)

val AdivinaTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    displayMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp,
        lineHeight = 52.sp
    ),
    displaySmall = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    titleMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    titleSmall = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.1.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    bodySmall = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    ),
    labelLarge = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),
    labelSmall = TextStyle(
        fontFamily = DynaPuffFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
