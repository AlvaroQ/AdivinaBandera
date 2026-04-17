package com.alvaroquintana.domain

data class GameModeDescriptor(
    val mode: GameMode,
    val unlockLevel: Int,
    val isUnlocked: Boolean,
    val unlockProgress: Float,
    val isNearUnlock: Boolean,
    val xpToUnlock: Int
)
