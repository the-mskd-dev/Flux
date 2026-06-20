package com.mskd.flux.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.mskd.flux.data.datastore.tokenDatastore
import com.mskd.flux.shared.di.Qualifiers
import org.koin.dsl.module

val moduleDatastoreAndroid = module {

    single<DataStore<Preferences>>(Qualifiers.TOKEN_DATASTORE) {
        get<Context>().tokenDatastore
    }

}