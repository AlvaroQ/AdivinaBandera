package com.alvaroquintana.adivinabandera.ui.main.components

import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.alvaroquintana.adivinabandera.ui.navigation.MainDestinations
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme

@Composable
fun MainBottomBar(
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val isDark = isAppInDarkTheme()
    val selectedColor = if (isDark) MaterialTheme.colorScheme.primary else GeoNavySoftLight

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        MainDestinations.entries.forEachIndexed { idx, dest ->
            NavigationBarItem(
                selected = idx == selectedIndex,
                onClick = { onTabSelected(idx) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    indicatorColor = selectedColor.copy(alpha = 0.18f),
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                icon = {
                    Icon(
                        imageVector = if (idx == selectedIndex) dest.iconSelected else dest.iconUnselected,
                        contentDescription = dest.label
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        fontFamily = DynaPuffFamily
                    )
                }
            )
        }
    }
}
