package com.mskd.flux.di

import com.mskd.flux.BuildConfig
import com.mskd.flux.model.AppInfo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object GlobalModule {

    @Provides
    @Singleton
    fun provideAppInfo() : AppInfo {
        return AppInfo(
            versionCode = BuildConfig.VERSION_CODE,
            versionName = BuildConfig.VERSION_NAME
        )
    }


}