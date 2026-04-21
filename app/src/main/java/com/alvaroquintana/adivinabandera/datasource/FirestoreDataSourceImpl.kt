package com.alvaroquintana.adivinabandera.datasource

import com.alvaroquintana.adivinabandera.utils.Constants.COLLECTION_RANKING
import com.alvaroquintana.adivinabandera.utils.Constants.COLLECTION_RANKING_CAPITAL_BY_FLAG
import com.alvaroquintana.adivinabandera.utils.Constants.COLLECTION_RANKING_SUBDIVISIONS
import com.alvaroquintana.data.datasource.FirestoreDataSource
import com.alvaroquintana.domain.User
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObjects
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class FirestoreDataSourceImpl : FirestoreDataSource {

    private val database = Firebase.firestore

    private fun collectionForMode(gameMode: String): String = when (gameMode) {
        "CapitalByFlag" -> COLLECTION_RANKING_CAPITAL_BY_FLAG
        "CurrencyDetective" -> "ranking-moneda"
        "PopulationChallenge" -> "ranking-poblacion"
        "WorldMix" -> "ranking-worldmix"
        "RegionSpain",
        "RegionMexico",
        "RegionArgentina",
        "RegionBrazil",
        "RegionGermany",
        "RegionUSA" -> COLLECTION_RANKING_SUBDIVISIONS
        else -> COLLECTION_RANKING
    }

    override suspend fun addRecord(user: User, gameMode: String): Result<User> {
        return suspendCancellableCoroutine { continuation ->
            database.collection(collectionForMode(gameMode))
                .add(user)
                .addOnSuccessListener {
                    continuation.resume(Result.success(user))
                }
                .addOnFailureListener {
                    FirebaseCrashlytics.getInstance().recordException(it)
                    continuation.resume(Result.failure(it))
                }
        }
    }

    override suspend fun getRanking(gameMode: String): MutableList<User> {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(collectionForMode(gameMode))
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(20)

            ref.get()
                .addOnSuccessListener {
                    continuation.resume(it.toObjects<User>().toMutableList())
                }
                .addOnFailureListener {
                    continuation.resume(mutableListOf())
                    FirebaseCrashlytics.getInstance().recordException(it)
                }
        }
    }

    override suspend fun getWorldRecords(limit: Long, gameMode: String): String {
        return suspendCancellableCoroutine { continuation ->
            val ref = database
                .collection(collectionForMode(gameMode))
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit)

            ref.get()
                .addOnSuccessListener {
                    val users = it.toObjects<User>()
                    continuation.resume(
                        if (users.isNotEmpty()) users.last().points else ""
                    )
                }
                .addOnFailureListener {
                    continuation.resume("")
                    FirebaseCrashlytics.getInstance().recordException(it)
                }
        }
    }
}
