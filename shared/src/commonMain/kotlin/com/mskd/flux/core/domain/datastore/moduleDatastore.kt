package com.mskd.flux.core.domain.datastore

import com.mskd.flux.core.data.datastore.CustomizationDataStore
import com.mskd.flux.core.data.datastore.SettingsDataStore
import com.mskd.flux.core.data.datastore.SnackbarDataStore
import com.mskd.flux.core.data.datastore.TokenDataStore
import com.mskd.flux.core.data.datastore.UserDataStore
import com.mskd.flux.di.Qualifiers
import org.koin.dsl.module

val moduleDatastore = module {

    single<CustomizationDataStore> {
        CustomizationDataStoreImpl(
            customizationDataStore = get(Qualifiers.CUSTOMIZATION_DATASTORE)
        )
    }

    single<SettingsDataStore> {
        SettingsDataStoreImpl(
            settingsDataStore = get(Qualifiers.SETTINGS_DATASTORE),
        )
    }

    single<SnackbarDataStore> {
        SnackbarDataStoreImpl(
            snackbarDataStore = get(Qualifiers.SNACKBAR_DATASTORE),
        )
    }

    single<TokenDataStore> {
        TokenDataStoreImpl(
            tokenDataStore = get(Qualifiers.TOKEN_DATASTORE)
        )
    }

    single<UserDataStore> {
        UserDataStoreImpl(
            userDataStore = get(Qualifiers.USER_DATASTORE),
            json = get()
        )
    }

}