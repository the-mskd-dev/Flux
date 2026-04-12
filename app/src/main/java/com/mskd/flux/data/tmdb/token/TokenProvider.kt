package com.mskd.flux.data.tmdb.token

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

val Context.tokenDatastore by preferencesDataStore(
    name = "TokenDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

interface TokenProvider {

    suspend fun getToken(): String?
    suspend fun saveToken(token: String)

    suspend fun dontRequestToken()

    val tokenRequested: Boolean

}