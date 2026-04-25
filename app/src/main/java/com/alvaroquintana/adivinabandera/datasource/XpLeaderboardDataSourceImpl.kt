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

@dev.zacsweers.metro.ContributesBinding(dev.zacsweers.metro.AppScope::class)
@dev.zacsweers.metro.Inject
class XpLeaderboardDataSourceImpl : XpLeaderboardDataSource {

    private val collection = Firebase.firestore.collection("xp-leaderboard-banderas")

    override suspend fun syncUserEntry(entry: XpLeaderboardEntry) {
        suspendCancellableCoroutine { continuation ->
            FirebaseCrashlytics.getInstance().log("xp_sync_user_entry_started")
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
                .addOnSuccessListener {
                    FirebaseCrashlytics.getInstance().log("xp_sync_user_entry_success")
                    continuation.resume(Unit)
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().log("xp_sync_user_entry_failed")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    continuation.resume(Unit)
                }
        }
    }

    override suspend fun getLeaderboard(limit: Int): List<XpLeaderboardEntry> {
        return suspendCancellableCoroutine { continuation ->
            FirebaseCrashlytics.getInstance().log("xp_leaderboard_load_started")
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
                        } catch (e: Exception) {
                            FirebaseCrashlytics.getInstance().apply {
                                log("xp_leaderboard_entry_parse_failed")
                                recordException(e)
                            }
                            null
                        }
                    }
                    FirebaseCrashlytics.getInstance().log("xp_leaderboard_load_success")
                    continuation.resume(entries)
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().log("xp_leaderboard_load_failed")
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
                        .addOnFailureListener { e ->
                            FirebaseCrashlytics.getInstance().log("xp_user_rank_count_failed")
                            FirebaseCrashlytics.getInstance().recordException(e)
                            continuation.resume(-1)
                        }
                }
                .addOnFailureListener { e ->
                    FirebaseCrashlytics.getInstance().log("xp_user_rank_load_failed")
                    FirebaseCrashlytics.getInstance().recordException(e)
                    continuation.resume(-1)
                }
        }
    }
}
