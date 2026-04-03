package com.mskd.flux.data.tmdb.token

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.mskd.flux.BuildConfig
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class TokenProviderImp(
    private val tokenDataStore: DataStore<Preferences>
) : TokenProvider {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("TMBD_TOKEN")
    }

    override suspend fun getToken(): String? {
        val token = tokenDataStore.data.map { it[TOKEN_KEY] }.first()
        return when {
            !token.isNullOrBlank() -> token
            BuildConfig.DEBUG -> BuildConfig.TMDB_TOKEN
            else -> null
        }
    }

    override suspend fun saveToken(token: String) {
        tokenDataStore.edit {
            it[TOKEN_KEY] = token
        }
    }

    override suspend fun clearToken() {
        tokenDataStore.edit { it[TOKEN_KEY] = "" }
    }

    override val hasToken: Boolean
        get() = runBlocking { !getToken().isNullOrBlank() }

}