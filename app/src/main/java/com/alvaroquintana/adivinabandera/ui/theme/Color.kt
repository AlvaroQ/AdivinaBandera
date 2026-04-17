package com.alvaroquintana.adivinabandera.ui.theme

import androidx.compose.ui.graphics.Color

// ── Game palette (flag/geography theme) ─────────────────────────────────────
val GameCream       = Color(0xFFFFF8E7)
val GameBlue        = Color(0xFF1A237E)   // patriotic deep blue — primary
val GameRed         = Color(0xFFC62828)   // rich red — secondary
val GameDark        = Color(0xFF1A2A3A)   // deep navy dark tone
val GameWhite       = Color(0xFFFFFFFF)
val GameMuted       = Color(0xFF78909C)   // blue-grey muted
val GameGreen       = Color(0xFF2ED573)   // correct answer green
val GameAnswerRed   = Color(0xFFFF4757)   // wrong answer red
val GameGold        = Color(0xFFF9CA24)   // streak gold
val GameSilver      = Color(0xFFC0C0C0)
val GameBronze      = Color(0xFFCD7F32)
val GameSky         = Color(0xFF87CEEB)
val GameLightBlue   = Color(0xFFD6EAF8)

// ── Geography UI palette ────────────────────────────────────────────────────
val GeoBackground   = Color(0xFFF0F4FF)   // azul cielo muy suave
val GeoNavy         = Color(0xFF1A3B6D)   // azul océano profundo
val GeoNavyLight    = Color(0xFF2B5EA7)   // azul medio para gradientes
val GeoNavySoft     = Color(0xFF3F6FAA)   // azul más claro para hero cards light
val GeoNavySoftLight= Color(0xFF6B9EDB)   // azul claro para gradientes light
val GeoForest       = Color(0xFF0D6B3D)   // verde oscuro (capitals)
val GeoForestLight  = Color(0xFF27AE60)   // verde medio
val GeoAmber        = Color(0xFFB7440E)   // naranja oscuro (countries)
val GeoAmberLight   = Color(0xFFE67E22)   // naranja medio
val GeoPurple       = Color(0xFF4A148C)   // morado oscuro (currency detective)
val GeoPurpleLight  = Color(0xFF7B1FA2)   // morado medio
val GeoTextPrimary  = Color(0xFF1A1A2E)   // texto principal light
val GeoTextSecondary= Color(0xFF5A6070)   // texto secundario light
val GeoTextMuted    = Color(0xFF9CA3AF)   // texto muted
val GeoBorder       = Color(0xFFE2E8F0)   // borde cards light
val GeoTeal         = Color(0xFF006064)   // teal oscuro (population challenge)
val GeoTealLight    = Color(0xFF00838F)   // teal medio
val DarkGeoBlue     = Color(0xFF4A90E2)   // azul primario dark mode
val DarkGeoBorder   = Color(0xFF2A3A55)   // borde cards dark
val GeoGold         = Color(0xFFE65100)   // gold deep (World Mix)
val GeoGoldLight    = Color(0xFFFF6D00)   // gold light (World Mix gradient)

// ── Dark theme palette (deep navy tones) ────────────────────────────────────
val DarkBackground  = Color(0xFF080D1C)   // profundo azul-negro — fondo principal
val DarkSurface     = Color(0xFF0E1628)   // azul marino oscuro — tarjetas/superficies
val DarkSurfaceVar  = Color(0xFF162035)   // azul marino medio — variante
val DarkOnSurface   = Color(0xFFD0DAEA)   // azul-blanco frío — texto primario
val DarkOnVariant   = Color(0xFF7896B8)   // azul grisáceo — texto secundario
val DarkAccent      = Color(0xFF3A5A7A)   // azul acero — bordes y accents
val DarkOutlineVar  = Color(0xFF1E3050)   // azul profundo — outline suave

// ── Dark surface container tiers (M3 expressive, navy-tinted) ───────────────
// Escalonados de más profundo (lowest) a más elevado (highest), todos con
// tinte navy para mantener coherencia con DarkSurface/DarkSurfaceVar.
val DarkSurfaceContainerLowest  = Color(0xFF0A1122)
val DarkSurfaceContainerLow     = Color(0xFF121C30)
val DarkSurfaceContainer        = Color(0xFF172338)
val DarkSurfaceContainerHigh    = Color(0xFF1E2B48)
val DarkSurfaceContainerHighest = Color(0xFF253357)

// ── Light palette ────────────────────────────────────────────────────────────
val md_theme_light_primary                = GameBlue
val md_theme_light_onPrimary              = GameWhite
val md_theme_light_primaryContainer       = Color(0xFFD3D9F8)
val md_theme_light_onPrimaryContainer     = Color(0xFF00084A)
val md_theme_light_secondary              = GameRed
val md_theme_light_onSecondary            = GameWhite
val md_theme_light_secondaryContainer     = Color(0xFFFFDAD6)
val md_theme_light_onSecondaryContainer   = Color(0xFF410002)
val md_theme_light_tertiary               = GameGreen
val md_theme_light_onTertiary             = GameWhite
val md_theme_light_tertiaryContainer      = Color(0xFFC8F7E8)
val md_theme_light_onTertiaryContainer    = Color(0xFF003D2D)
val md_theme_light_error                  = GameAnswerRed
val md_theme_light_onError                = GameWhite
val md_theme_light_errorContainer         = Color(0xFFFFDAD6)
val md_theme_light_onErrorContainer       = Color(0xFF410002)
val md_theme_light_background             = GameWhite
val md_theme_light_onBackground           = GameDark
val md_theme_light_surface                = GameWhite
val md_theme_light_onSurface              = GameDark
val md_theme_light_surfaceVariant         = Color(0xFFE8EDF5)
val md_theme_light_onSurfaceVariant       = GameMuted
val md_theme_light_outline                = GameMuted
val md_theme_light_outlineVariant         = Color(0xFFCDD5DF)
val md_theme_light_inverseSurface         = GameDark
val md_theme_light_inverseOnSurface       = GameCream
val md_theme_light_inversePrimary         = Color(0xFFADB8F0)
val md_theme_light_surfaceTint            = GameBlue
val md_theme_light_scrim                  = Color(0xFF000000)

// ── Dark palette ─────────────────────────────────────────────────────────────
val md_theme_dark_primary                 = Color(0xFFADB8F0)  // azul claro sobre fondo oscuro
val md_theme_dark_onPrimary               = Color(0xFF001270)
val md_theme_dark_primaryContainer        = Color(0xFF001DA8)
val md_theme_dark_onPrimaryContainer      = Color(0xFFD3D9F8)
val md_theme_dark_secondary               = Color(0xFFFFB4AB)
val md_theme_dark_onSecondary             = Color(0xFF690005)
val md_theme_dark_secondaryContainer      = DarkSurfaceVar
val md_theme_dark_onSecondaryContainer    = DarkOnSurface
val md_theme_dark_tertiary                = Color(0xFF7DDFCA)
val md_theme_dark_onTertiary              = Color(0xFF003D2D)
val md_theme_dark_tertiaryContainer       = Color(0xFF005140)
val md_theme_dark_onTertiaryContainer     = Color(0xFFC8F7E8)
val md_theme_dark_error                   = Color(0xFFFF6B6B)
val md_theme_dark_onError                 = Color(0xFF690005)
val md_theme_dark_errorContainer          = Color(0xFF93000A)
val md_theme_dark_onErrorContainer        = Color(0xFFFFDAD6)
val md_theme_dark_background              = DarkSurface
val md_theme_dark_onBackground            = DarkOnSurface
val md_theme_dark_surface                 = DarkSurface
val md_theme_dark_onSurface               = DarkOnSurface
val md_theme_dark_surfaceVariant          = DarkSurfaceVar
val md_theme_dark_onSurfaceVariant        = DarkOnVariant
val md_theme_dark_outline                 = DarkAccent
val md_theme_dark_outlineVariant          = DarkSurfaceVar
val md_theme_dark_inverseSurface          = DarkOnSurface
val md_theme_dark_inverseOnSurface        = DarkBackground
val md_theme_dark_inversePrimary          = Color(0xFF1A237E)
val md_theme_dark_surfaceTint             = Color(0xFFADB8F0)
val md_theme_dark_scrim                   = Color(0xFF000000)
