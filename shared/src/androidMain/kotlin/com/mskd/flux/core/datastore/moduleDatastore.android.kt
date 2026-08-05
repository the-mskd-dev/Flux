package com.mskd.flux.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mskd.flux.di.Qualifiers
import org.koin.dsl.module

val moduleDatastoreAndroid = module {

    single<DataStore<Preferences>>(Qualifiers.CATALOG_DATASTORE) {
        get<Context>().catalogDataStore
    }

    single<DataStore<Preferences>>(Qualifiers.CUSTOMIZATION_DATASTORE) {
        get<Context>().customizationDatastore
    }

    single<DataStore<Preferences>>(Qualifiers.SETTINGS_DATASTORE) {
        get<Context>().settingsDatastore
    }

    single<DataStore<Preferences>>(Qualifiers.SNACKBAR_DATASTORE) {
        get<Context>().snackbarDataStore
    }

    single<DataStore<Preferences>>(Qualifiers.TOKEN_DATASTORE) {
        get<Context>().tokenDatastore
    }

    single<DataStore<Preferences>>(Qualifiers.USER_DATASTORE) {
        get<Context>().userDataStore
    }

}