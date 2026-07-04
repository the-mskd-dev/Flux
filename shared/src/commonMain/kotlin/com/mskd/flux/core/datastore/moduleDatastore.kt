package com.mskd.flux.core.datastore

import com.mskd.flux.core.datastore.customization.CustomizationDataStore
import com.mskd.flux.core.datastore.customization.CustomizationDataStoreImpl
import com.mskd.flux.core.datastore.settings.SettingsDataStore
import com.mskd.flux.core.datastore.settings.SettingsDataStoreImpl
import com.mskd.flux.core.datastore.snackbars.SnackbarDataStore
import com.mskd.flux.core.datastore.snackbars.SnackbarDataStoreImpl
import com.mskd.flux.core.datastore.token.TokenDataStore
import com.mskd.flux.core.datastore.token.TokenDataStoreImpl
import com.mskd.flux.core.datastore.user.UserDataStore
import com.mskd.flux.core.datastore.user.UserDataStoreImpl
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