package com.alvaroquintana.adivinabandera.managers

import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.CountryStats
import com.alvaroquintana.usecases.engagement.CountryMasteryService

class CountryMasteryManager(private val dataBaseSource: DataBaseSource) : CountryMasteryService {

    data class RegionMastery(
        val region: String,
        val totalCountries: Int,
        val discoveredCountries: Int,
        val masteryPercent: Float
    )

    /**
     * Registra si la respuesta fue correcta o no para un pais.
     * Actualiza timesCorrect/timesWrong y lastSeenTimestamp en la DB.
     * La primera vez que se acierta se setea firstDiscoveredTimestamp.
     */
    override suspend fun recordAnswer(alpha2Code: String, isCorrect: Boolean, gameMode: String) {
        val existing = dataBaseSource.getCountryStatByCode(alpha2Code)
        val now = System.currentTimeMillis()
        val stat = CountryStats(
            alpha2Code = alpha2Code,
            timesCorrect = (existing?.timesCorrect ?: 0) + if (isCorrect) 1 else 0,
            timesWrong = (existing?.timesWrong ?: 0) + if (!isCorrect) 1 else 0,
            firstDiscoveredTimestamp = existing?.firstDiscoveredTimestamp
                ?: if (isCorrect) now else 0L,
            lastSeenTimestamp = now,
            lastSeenInMode = gameMode
        )
        dataBaseSource.upsertCountryStat(stat)
    }

    /** Cantidad de paises que el jugador ya respondio correctamente al menos una vez. */
    suspend fun getDiscoveredCount(): Int = dataBaseSource.getDiscoveredCountriesCount()

    /**
     * Retorna los paises con mas errores acumulados — utiles para recomendarlos en el futuro.
     * [limit] define cuantos retornar (default 10).
     */
    suspend fun getWeakSpots(limit: Int = 10): List<CountryStats> =
        dataBaseSource.getTopWrongCountries(limit)

    /**
     * Retorna los IDs de DB de los paises con mas errores acumulados.
     * Usado para pasar el pool forzado al modo practica.
     */
    suspend fun getWeakSpotsAsIds(limit: Int = 10): List<Int> {
        return dataBaseSource.getTopWrongCountries(limit).mapNotNull { stat ->
            dataBaseSource.getCountryIdByAlpha2Code(stat.alpha2Code)
        }
    }
}
