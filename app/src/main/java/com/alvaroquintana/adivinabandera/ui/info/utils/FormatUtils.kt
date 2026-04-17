package com.alvaroquintana.adivinabandera.ui.info.utils

import java.text.NumberFormat
import java.util.Locale
import kotlin.math.abs

fun formatPopulationShort(population: Int): String = when {
    population >= 1_000_000_000 -> "%.1fB".format(population / 1_000_000_000.0)
    population >= 1_000_000 -> "%.1fM".format(population / 1_000_000.0)
    population >= 1_000 -> "%.0fK".format(population / 1_000.0)
    population > 0 -> population.toString()
    else -> "—"
}

fun formatPopulationFull(population: Int): String =
    if (population > 0) NumberFormat.getNumberInstance(Locale.getDefault()).format(population)
    else "—"

fun formatArea(area: Int): String =
    if (area > 0) NumberFormat.getNumberInstance(Locale.getDefault()).format(area)
    else "—"

fun computeDensity(population: Int, area: Int): String =
    if (area > 0 && population > 0) {
        val density = population.toDouble() / area.toDouble()
        NumberFormat.getNumberInstance(Locale.getDefault()).format(density.toInt())
    } else "—"

fun formatCoordinates(latlng: List<Double>): String {
    if (latlng.size < 2) return "—"
    val lat = latlng[0]
    val lng = latlng[1]
    val latDir = if (lat >= 0) "N" else "S"
    val lngDir = if (lng >= 0) "E" else "W"
    return "%.2f°%s, %.2f°%s".format(abs(lat), latDir, abs(lng), lngDir)
}
