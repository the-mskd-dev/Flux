package com.mskd.flux.data.tmdb.token

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

val Context.tokenDatastore by preferencesDataStore(
    name = "TokenDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

interface TokenRepository {

    val flow: Flow<String>

    suspend fun getToken(): String
    suspend fun saveToken(token: String)

    suspend fun clearToken()

    suspend fun dontRequestToken()

    val tokenRequested: Boolean

}