package com.mskd.flux.core.datastore

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

val Context.catalogDataStore by preferencesDataStore(
    name = "CatalogDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

val Context.customizationDatastore by preferencesDataStore(
    name = "CustomizationDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

val Context.settingsDatastore by preferencesDataStore(
    name = "SettingsDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

val Context.snackbarDataStore by preferencesDataStore(
    name ="SnackbarDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

val Context.tokenDatastore by preferencesDataStore(
    name = "TokenDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)

val Context.userDataStore by preferencesDataStore(
    name ="UserDataStore",
    corruptionHandler = ReplaceFileCorruptionHandler(
        produceNewData = { emptyPreferences() }
    )
)