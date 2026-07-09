package com.mskd.flux.core.domain.datastore

import com.mskd.flux.core.data.datastore.SnackbarDataStoreImpl
import com.mskd.flux.core.data.datastore.UserDataStoreImpl
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