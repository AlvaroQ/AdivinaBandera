package com.alvaroquintana.domain

enum class Achievement(
    val id: String,
    val xpReward: Int,
    val icon: String
) {
    // Hitos de partidas
    FIRST_GAME("first_game", 50, "\uD83C\uDFAF"),
    TEN_GAMES("ten_games", 100, "\uD83D\uDD1F"),
    FIFTY_GAMES("fifty_games", 250, "\u2B50"),
    HUNDRED_GAMES("hundred_games", 500, "\uD83D\uDCAF"),

    // Partidas perfectas
    FIRST_PERFECT("first_perfect", 100, "\u2728"),
    FIVE_PERFECT("five_perfect", 300, "\uD83D\uDC8E"),

    // Rachas
    STREAK_5("streak_5", 50, "\uD83D\uDD25"),
    STREAK_10("streak_10", 150, "\uD83D\uDCA5"),
    STREAK_15("streak_15", 300, "\u26A1"),
    STREAK_20("streak_20", 500, "\uD83C\uDF1F"),

    // Niveles
    LEVEL_10("level_10", 200, "\uD83D\uDCC8"),
    LEVEL_25("level_25", 500, "\uD83C\uDFD4\uFE0F"),
    LEVEL_50("level_50", 1000, "\uD83D\uDC51"),

    // Especiales
    SPEED_DEMON("speed_demon", 300, "\u23F1\uFE0F"),
    DEDICATED("dedicated", 200, "\uD83D\uDCDA"),
    ACCURACY_80("accuracy_80", 250, "\uD83C\uDFAF"),
    ACCURACY_90("accuracy_90", 500, "\uD83C\uDFC6"),

    // Rachas diarias (daily streak)
    STREAK_DAILY_7("streak_daily_7", 200, "\uD83D\uDD25"),
    STREAK_DAILY_14("streak_daily_14", 500, "\uD83D\uDD25"),
    STREAK_DAILY_30("streak_daily_30", 1000, "\uD83D\uDD25"),
    STREAK_DAILY_60("streak_daily_60", 2500, "\uD83D\uDD25"),
    STREAK_DAILY_90("streak_daily_90", 5000, "\uD83D\uDD25"),
    STREAK_DAILY_365("streak_daily_365", 10000, "\uD83D\uDD25")
}
