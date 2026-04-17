package com.alvaroquintana.adivinabandera.datasource

import com.alvaroquintana.data.datasource.XpLeaderboardDataSource
import com.alvaroquintana.domain.XpLeaderboardEntry
import com.google.firebase.Firebase
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class XpLeaderboardDataSourceImpl : XpLeaderboardDataSource {

    private val collection = Firebase.firestore.collection("xp-leaderboard-banderas")

    override suspend fun syncUserEntry(entry: XpLeaderboardEntry) {
        suspendCancellableCoroutine { continuation ->
            val data = mapOf(
                "uid" to entry.uid,
                "nickname" to entry.nickname,
                "imageBase64" to entry.imageBase64,
                "totalXp" to entry.totalXp,
                "level" to entry.level,
                "title" to entry.title,
                "totalGamesPlayed" to entry.totalGamesPlayed,
                "accuracy" to entry.accuracy,
                "lastUpdated" to FieldValue.serverTimestamp()
            )

            collection.document(entry.uid)
                .set(data, SetOptions.merge())
                .addOnSuccessListener { continuation.resume(Unit) }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    continuation.resume(Unit)
                }
        }
    }

    override suspend fun getLeaderboard(limit: Int): List<XpLeaderboardEntry> {
        return suspendCancellableCoroutine { continuation ->
            collection
                .orderBy("totalXp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .addOnSuccessListener { snapshot ->
                    val entries = snapshot.documents.mapNotNull { doc ->
                        try {
                            XpLeaderboardEntry(
                                uid = doc.getString("uid") ?: "",
                                nickname = doc.getString("nickname") ?: "",
                                imageBase64 = doc.getString("imageBase64") ?: "",
                                totalXp = (doc.getLong("totalXp") ?: 0).toInt(),
                                level = (doc.getLong("level") ?: 1).toInt(),
                                title = doc.getString("title") ?: "",
                                totalGamesPlayed = (doc.getLong("totalGamesPlayed") ?: 0).toInt(),
                                accuracy = (doc.getDouble("accuracy") ?: 0.0).toFloat(),
                                lastUpdated = doc.getTimestamp("lastUpdated")?.toDate()?.time ?: 0L
                            )
                        } catch (_: Exception) {
                            null
                        }
                    }
                    continuation.resume(entries)
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    continuation.resume(emptyList())
                }
        }
    }

    override suspend fun getUserRank(uid: String): Int {
        return suspendCancellableCoroutine { continuation ->
            collection.document(uid).get()
                .addOnSuccessListener { doc ->
                    val userXp = (doc.getLong("totalXp") ?: 0).toInt()
                    collection
                        .whereGreaterThan("totalXp", userXp)
                        .count()
                        .get(AggregateSource.SERVER)
                        .addOnSuccessListener { result ->
                            continuation.resume(result.count.toInt() + 1)
                        }
                        .addOnFailureListener {
                            continuation.resume(-1)
                        }
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().recordException(e)
                    continuation.resume(-1)
                }
        }
    }
}
