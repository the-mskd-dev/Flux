package com.mskd.flux.core.datastore.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import com.mskd.flux.core.datastore.domain.SnackbarDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SnackbarDataStoreImpl(val snackbarDataStore: DataStore<Preferences>) : SnackbarDataStore {

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