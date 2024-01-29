package com.kaem.flux.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

class DataStoreRepository(private val dataStore: DataStore<Preferences>) {
}