package com.alvaroquintana.domain

sealed class GameMode {
    open val unlockLevel: Int = 0
    object Classic : GameMode()
    object CapitalByFlag : GameMode()
    object CapitalByCountry : GameMode()
    object CurrencyDetective : GameMode() { override val unlockLevel = 5 }
    object PopulationChallenge : GameMode() { override val unlockLevel = 10 }
    object WorldMix : GameMode() { override val unlockLevel = 15 }
}

fun GameMode.toRouteString(): String = when (this) {
    is GameMode.Classic -> "Classic"
    is GameMode.CapitalByFlag -> "CapitalByFlag"
    is GameMode.CapitalByCountry -> "CapitalByCountry"
    is GameMode.CurrencyDetective -> "CurrencyDetective"
    is GameMode.PopulationChallenge -> "PopulationChallenge"
    is GameMode.WorldMix -> "WorldMix"
}

fun String.toGameMode(): GameMode = when (this) {
    "CapitalByFlag" -> GameMode.CapitalByFlag
    "CapitalByCountry" -> GameMode.CapitalByCountry
    "CurrencyDetective" -> GameMode.CurrencyDetective
    "PopulationChallenge" -> GameMode.PopulationChallenge
    "WorldMix" -> GameMode.WorldMix
    else -> GameMode.Classic
}
