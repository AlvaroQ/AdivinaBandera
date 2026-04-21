package com.alvaroquintana.domain

sealed class GameMode {
    open val unlockLevel: Int = 0
    object Classic : GameMode()
    object CapitalByFlag : GameMode()

    // Chain de modos regionales: cada uno se desbloquea acertando en el anterior.
    // ES siempre abierto -> MX -> AR -> BR -> DE -> US
    object RegionSpain : GameMode()
    object RegionMexico : GameMode()
    object RegionArgentina : GameMode()
    object RegionBrazil : GameMode()
    object RegionGermany : GameMode()
    object RegionUSA : GameMode()

    object CurrencyDetective : GameMode() { override val unlockLevel = 5 }
    object PopulationChallenge : GameMode() { override val unlockLevel = 10 }
    object WorldMix : GameMode() { override val unlockLevel = 15 }
}

/** Aciertos requeridos en el modo previo para desbloquear el siguiente eslabón del chain regional. */
const val REGIONAL_UNLOCK_THRESHOLD: Int = 6

/** Alpha2 ISO para los modos regionales. Null para modos no regionales. */
val GameMode.regionalAlpha2: String?
    get() = when (this) {
        is GameMode.RegionSpain -> "ES"
        is GameMode.RegionMexico -> "MX"
        is GameMode.RegionArgentina -> "AR"
        is GameMode.RegionBrazil -> "BR"
        is GameMode.RegionGermany -> "DE"
        is GameMode.RegionUSA -> "US"
        else -> null
    }

val GameMode.isRegional: Boolean get() = regionalAlpha2 != null

/** Eslabón previo del chain regional; null para el primero (España) o modos no regionales. */
val GameMode.regionalPrerequisite: GameMode?
    get() = when (this) {
        is GameMode.RegionSpain -> null
        is GameMode.RegionMexico -> GameMode.RegionSpain
        is GameMode.RegionArgentina -> GameMode.RegionMexico
        is GameMode.RegionBrazil -> GameMode.RegionArgentina
        is GameMode.RegionGermany -> GameMode.RegionBrazil
        is GameMode.RegionUSA -> GameMode.RegionGermany
        else -> null
    }

/** Cadena ordenada de modos regionales (para UI y recorrido). */
val regionalChain: List<GameMode> = listOf(
    GameMode.RegionSpain,
    GameMode.RegionMexico,
    GameMode.RegionArgentina,
    GameMode.RegionBrazil,
    GameMode.RegionGermany,
    GameMode.RegionUSA
)

fun GameMode.toRouteString(): String = when (this) {
    is GameMode.Classic -> "Classic"
    is GameMode.CapitalByFlag -> "CapitalByFlag"
    is GameMode.RegionSpain -> "RegionSpain"
    is GameMode.RegionMexico -> "RegionMexico"
    is GameMode.RegionArgentina -> "RegionArgentina"
    is GameMode.RegionBrazil -> "RegionBrazil"
    is GameMode.RegionGermany -> "RegionGermany"
    is GameMode.RegionUSA -> "RegionUSA"
    is GameMode.CurrencyDetective -> "CurrencyDetective"
    is GameMode.PopulationChallenge -> "PopulationChallenge"
    is GameMode.WorldMix -> "WorldMix"
}

fun String.toGameMode(): GameMode = when (this) {
    "CapitalByFlag" -> GameMode.CapitalByFlag
    "RegionSpain" -> GameMode.RegionSpain
    "RegionMexico" -> GameMode.RegionMexico
    "RegionArgentina" -> GameMode.RegionArgentina
    "RegionBrazil" -> GameMode.RegionBrazil
    "RegionGermany" -> GameMode.RegionGermany
    "RegionUSA" -> GameMode.RegionUSA
    "CurrencyDetective" -> GameMode.CurrencyDetective
    "PopulationChallenge" -> GameMode.PopulationChallenge
    "WorldMix" -> GameMode.WorldMix
    else -> GameMode.Classic
}
