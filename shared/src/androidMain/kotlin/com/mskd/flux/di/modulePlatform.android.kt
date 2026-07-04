package com.mskd.flux.di


import com.mskd.flux.core.moduleCoreAndroid
import com.mskd.flux.features.moduleFeaturesAndroid
import com.mskd.flux.model.core.AppInfo
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
    single<AppInfo> {
        AppInfo(
            versionCode = getProperty(Properties.VERSION_CODE),
            versionName = getProperty(Properties.VERSION_NAME),
            isDebug = getProperty(Properties.IS_DEBUG),
            debugToken = getProperty(Properties.DEBUG_TOKEN),
        )
    }

}