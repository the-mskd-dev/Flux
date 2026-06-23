package com.mskd.flux.di


import com.mskd.flux.model.AppInfo
import com.mskd.flux.platform.AndroidImageRequestFactory
import com.mskd.flux.platform.AndroidMetadataProvider
import com.mskd.flux.platform.ImageRequestFactory
import com.mskd.flux.platform.MetadataProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val modulePlatform: Module = module {

    // Common
    includes(moduleCommon)

    // Android
    includes(
        moduleDatabaseAndroid,
        moduleDatastoreAndroid,
        moduleImagesAndroid,
        modulePlayerAndroid,
        moduleRepositoryAndroid,
        moduleUseCaseAndroid
    )

    single<MetadataProvider> {
        AndroidMetadataProvider(context = androidContext())
    }

    single<ImageRequestFactory> {
        AndroidImageRequestFactory(context = androidContext())
    }

    single<AppInfo> {
        AppInfo(
            versionCode = getProperty(Properties.VERSION_CODE),
            versionName = getProperty(Properties.VERSION_NAME),
            isDebug = getProperty(Properties.IS_DEBUG),
            debugToken = getProperty(Properties.DEBUG_TOKEN),
        )
    }

}