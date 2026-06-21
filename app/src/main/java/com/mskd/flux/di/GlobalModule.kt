package com.mskd.flux.di

import com.mskd.flux.BuildConfig
import com.mskd.flux.model.AppInfo
import org.koin.dsl.module

val globalModule = module {

    single<AppInfo> {
        AppInfo(
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME
        )
    }

}