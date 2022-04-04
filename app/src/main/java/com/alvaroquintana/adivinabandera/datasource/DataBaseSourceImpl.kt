package com.alvaroquintana.adivinabandera.datasource

import com.alvaroquintana.adivinabandera.BuildConfig
import com.alvaroquintana.adivinabandera.utils.Constants.PATH_REFERENCE_COUNTRIES
import com.alvaroquintana.adivinabandera.utils.Constants.PATH_REFERENCE_APPS
import com.alvaroquintana.adivinabandera.utils.Constants.TOTAL_ITEM_EACH_LOAD
import com.alvaroquintana.data.datasource.DataBaseSource
import com.alvaroquintana.domain.Country
import com.alvaroquintana.adivinabandera.utils.log
import com.alvaroquintana.domain.App
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import com.google.firebase.database.ktx.getValue
import com.google.firebase.crashlytics.FirebaseCrashlytics

class DataBaseSourceImpl : DataBaseSource {

    override suspend fun getCountryById(id: Int): Country {
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_COUNTRIES + id)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        continuation.resume(dataSnapshot.getValue(Country::class.java) as Country)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        log("getCountryById FAILED", "Failed to read value.", error.toException())
                        continuation.resume(Country())
                        FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                    }
                })
        }
    }

    override suspend fun getCountryList(currentPage: Int): MutableList<Country> {
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_COUNTRIES)
                .orderByKey()
                .startAt((currentPage * TOTAL_ITEM_EACH_LOAD).toString())
                .limitToFirst(TOTAL_ITEM_EACH_LOAD)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        val countryList = mutableListOf<Country>()
                        if(dataSnapshot.hasChildren()) {
                            for(snapshot in dataSnapshot.children) {
                                countryList.add(snapshot.getValue(Country::class.java)!!)
                            }
                        }
                        continuation.resume(countryList) {}
                    }

                    override fun onCancelled(error: DatabaseError) {
                        log("DataBaseBaseSourceImpl", "Failed to read value.", error.toException())
                        continuation.resume(mutableListOf()){}
                        FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                    }
                })
        }
    }

    override suspend fun getAppsRecommended(): MutableList<App> {
        return suspendCancellableCoroutine { continuation ->
            FirebaseDatabase.getInstance().getReference(PATH_REFERENCE_APPS)
                .addValueEventListener(object : ValueEventListener {

                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        var value = dataSnapshot.getValue<MutableList<App>>()
                        if(value == null) value = mutableListOf()
                        continuation.resume(value.filter { !it.url!!.contains(BuildConfig.APPLICATION_ID) }.toMutableList())
                    }

                    override fun onCancelled(error: DatabaseError) {
                        log("DataBaseBaseSourceImpl", "Failed to read value.", error.toException())
                        continuation.resume(mutableListOf())
                        FirebaseCrashlytics.getInstance().recordException(Throwable(error.toException()))
                    }
                })
        }
    }
}