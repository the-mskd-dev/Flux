package com.mskd.flux.data.tmdb.token

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class TokenProviderImp(
    private val tokenDataStore: DataStore<Preferences>
) : TokenProvider {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("TMBD_TOKEN")
        private val REQUEST_TOKEN = booleanPreferencesKey("REQUEST_TOKEN")
    }

    override suspend fun getToken(): String? {
        val token = tokenDataStore.data.map { it[TOKEN_KEY] }.first()
        return when {
            !token.isNullOrBlank() -> token
            //BuildConfig.DEBUG -> BuildConfig.TMDB_TOKEN
            else -> null
        }
    }

    override suspend fun saveToken(token: String) {
        tokenDataStore.edit {
            it[TOKEN_KEY] = token
            it[REQUEST_TOKEN] = false
        }
    }

    override suspend fun dontRequestToken() {
        tokenDataStore.edit {
            it[REQUEST_TOKEN] = false
        }
    }

    override val tokenRequested: Boolean
        get() = runBlocking {
            tokenDataStore.data.map { it[REQUEST_TOKEN] }.first() ?: true
        }

}