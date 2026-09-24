package com.mskd.flux.di


import com.mskd.flux.core.moduleCoreAndroid
import com.mskd.flux.features.moduleFeaturesAndroid
import com.mskd.flux.platform.AndroidEmailLauncher
import com.mskd.flux.platform.EmailLauncher
import org.koin.core.module.Module
import org.koin.dsl.module

actual val modulePlatform: Module = module {

    // Common
    includes(moduleCommon)

    // Android
    includes(
        moduleCoreAndroid,
        moduleFeaturesAndroid,
    )

    single<EmailLauncher> { AndroidEmailLauncher(get()) }

}