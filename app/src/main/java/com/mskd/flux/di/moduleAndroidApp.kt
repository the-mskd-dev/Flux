package com.mskd.flux.di

import com.mskd.flux.BuildConfig
import com.mskd.flux.MainViewModel
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.core.Flavor
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleAndroidApp = module {

    single<AppInfo> {
        AppInfo(
            isDebug = BuildConfig.DEBUG,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            baseToken = BuildConfig.TMDB_TOKEN,
            flavor = Flavor.fromString(BuildConfig.FLAVOR)
        )
    }

    viewModelOf(::MainViewModel)

}