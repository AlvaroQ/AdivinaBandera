package com.alvaroquintana.adivinabandera.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestinations(
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector
) {
    Select(
        label = "Jugar",
        iconSelected = Icons.Filled.SportsEsports,
        iconUnselected = Icons.Outlined.SportsEsports
    ),
    Info(
        label = "Explorar",
        iconSelected = Icons.Filled.Flag,
        iconUnselected = Icons.Outlined.Flag
    ),
    Ranking(
        label = "Ranking",
        iconSelected = Icons.Filled.Leaderboard,
        iconUnselected = Icons.Outlined.Leaderboard
    ),
    Profile(
        label = "Perfil",
        iconSelected = Icons.Filled.Person,
        iconUnselected = Icons.Outlined.Person
    )
}
