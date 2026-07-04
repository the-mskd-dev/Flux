package com.mskd.flux.di

import com.mskd.flux.data.useCases.files.FilesUC
import com.mskd.flux.data.useCases.files.FilesUCImpl
import com.mskd.flux.data.useCases.player.PipIsEnabledUC
import com.mskd.flux.data.useCases.player.PipIsEnabledUCImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleUseCaseAndroid = module {

    single<PipIsEnabledUC> {
        PipIsEnabledUCImpl(
            context = androidContext(),
            settingsDataStore = get()
        )
    }

    single<FilesUC> {
        FilesUCImpl(
            mediaStoreRepository = get(QualifiersAndroid.MEDIASTORE_SOURCES),
            safRepository = get(QualifiersAndroid.SAF_SOURCES)
        )
    }

}