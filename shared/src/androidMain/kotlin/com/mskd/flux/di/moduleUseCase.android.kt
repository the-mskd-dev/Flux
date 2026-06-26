package com.mskd.flux.di

import com.mskd.flux.useCases.player.PipIsEnabledUC
import com.mskd.flux.useCases.player.PipIsEnabledUCImpl
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleUseCaseAndroid = module {

    single<PipIsEnabledUC> {
        PipIsEnabledUCImpl(
            context = androidContext(),
            settingsRepository = get()
        )
    }

}