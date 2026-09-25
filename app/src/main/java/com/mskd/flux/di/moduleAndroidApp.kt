package com.mskd.flux.di

import com.mskd.flux.BuildConfig
import com.mskd.flux.MainViewModel
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.core.Flavor
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val moduleAndroidApp = module {

    single<AppInfo> {

        val flavor = Flavor.fromString(BuildConfig.FLAVOR)

        AppInfo(
            isDebug = BuildConfig.DEBUG,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE,
            baseToken = if (BuildConfig.DEBUG || flavor == Flavor.PLAY_STORE) BuildConfig.TMDB_TOKEN else "",
            flavor = flavor
        )
        
    }

    viewModelOf(::MainViewModel)

}