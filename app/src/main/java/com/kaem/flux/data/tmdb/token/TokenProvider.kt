package com.kaem.flux.data.tmdb.token

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

interface TokenProvider {


    suspend fun getToken(): String?
    suspend fun saveToken(token: String)
    suspend fun clearToken()
    val hasToken: Boolean
}