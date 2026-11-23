package com.mskd.flux.di

import com.mskd.flux.BuildConfig
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.core.Flavor
import com.mskd.flux.external.AndroidExternalPlayerAdapter
import com.mskd.flux.features.player.domain.playback.ExternalPlayerAdapter
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.bind
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

    // MainActivity needs the concrete type to attach its launcher; PlaybackLaunch only sees the interface.
    single { AndroidExternalPlayerAdapter(context = androidContext()) } bind ExternalPlayerAdapter::class

}
