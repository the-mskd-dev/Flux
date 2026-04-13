package com.mskd.flux.data.repository.snackbars

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow

val Context.snackbarDataStore by preferencesDataStore(
    name ="SnackbarDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

interface SnackbarRepository {

    fun canShow(snackbarId: String) : Flow<Boolean>

    fun getCount(snackbarId: String) : Flow<Int>

    suspend fun incrementCount(snackbarId: String)

}