package com.kaem.flux.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import javax.inject.Inject


class DataStoreRepository @Inject constructor(private val dataStore: DataStore<Preferences>) {
}