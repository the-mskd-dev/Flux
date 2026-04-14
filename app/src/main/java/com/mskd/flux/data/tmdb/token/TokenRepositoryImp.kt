package com.mskd.flux.data.tmdb.token

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.io.IOException

class TokenRepositoryImp(
    private val tokenDataStore: DataStore<Preferences>
) : TokenRepository {

    companion object {
        private val TOKEN_KEY = stringPreferencesKey("TMBD_TOKEN")
        private val REQUEST_TOKEN = booleanPreferencesKey("REQUEST_TOKEN")
    }

    override val flow: Flow<String> = tokenDataStore.data
        .catch { exception -> if (exception is IOException) emit(emptyPreferences()) else throw exception }
        .map { it[TOKEN_KEY] ?: "" }

    override suspend fun getToken(): String {
        return tokenDataStore.data.map { it[TOKEN_KEY] }.first() ?: ""
    }

    override suspend fun clearToken() {
        tokenDataStore.edit {
            it[TOKEN_KEY] = ""
            it[REQUEST_TOKEN] = false
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