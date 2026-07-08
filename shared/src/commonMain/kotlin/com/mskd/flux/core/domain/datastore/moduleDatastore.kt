package com.mskd.flux.core.domain.datastore

import com.mskd.flux.core.data.datastore.CustomizationDataStoreImpl
import com.mskd.flux.core.data.datastore.SettingsDataStoreImpl
import com.mskd.flux.core.data.datastore.SnackbarDataStoreImpl
import com.mskd.flux.core.data.datastore.TokenDataStoreImpl
import com.mskd.flux.core.data.datastore.UserDataStoreImpl
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