package com.mskd.flux.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mskd.flux.data.datastore.customizationDatastore
import com.mskd.flux.data.datastore.settingsDatastore
import com.mskd.flux.data.datastore.snackbarDataStore
import com.mskd.flux.data.datastore.tokenDatastore
import com.mskd.flux.data.datastore.userDataStore
import org.koin.dsl.module

val moduleDatastoreAndroid = module {

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