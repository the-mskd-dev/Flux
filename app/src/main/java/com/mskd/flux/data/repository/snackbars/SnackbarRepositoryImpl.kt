package com.mskd.flux.data.repository.snackbars

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SnackbarRepositoryImpl(val snackbarDataStore: DataStore<Preferences>) : SnackbarRepository {

    override fun canShow(snackbarId: String): Flow<Boolean> {
        return snackbarDataStore.data
            .map { preferences -> (preferences[intPreferencesKey(snackbarId)] ?: 0) < 3 }
    }

    override fun getCount(snackbarId: String) : Flow<Int> {
        return snackbarDataStore.data
            .map { preferences -> preferences[intPreferencesKey(snackbarId)] ?: 0 }
    }

    override suspend fun incrementCount(snackbarId: String) {
        snackbarDataStore.edit { preferences ->
            val current = preferences[intPreferencesKey(snackbarId)] ?: 0
            preferences[intPreferencesKey(snackbarId)] = current + 1
        }
    }

}