package com.mskd.flux.core.datastore

import com.mskd.flux.core.datastore.data.SnackbarDataStoreImpl
import com.mskd.flux.core.datastore.data.UserDataStoreImpl
import com.mskd.flux.core.datastore.domain.SnackbarDataStore
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.di.Qualifiers
import org.koin.dsl.module

val moduleDatastore = module {

    single<SnackbarDataStore> {
        SnackbarDataStoreImpl(
            snackbarDataStore = get(Qualifiers.SNACKBAR_DATASTORE),
        )
    }

    single<UserDataStore> {
        UserDataStoreImpl(
            userDataStore = get(Qualifiers.USER_DATASTORE),
            json = get()
        )
    }

}