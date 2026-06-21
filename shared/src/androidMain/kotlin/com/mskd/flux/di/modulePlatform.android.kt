package com.mskd.flux.di

import coil3.PlatformContext
import com.mskd.flux.platform.AndroidImageRequestFactory
import com.mskd.flux.platform.AndroidMetadataProvider
import com.mskd.flux.platform.ImageRequestFactory
import com.mskd.flux.platform.MetadataProvider
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val modulePlatform: Module = module {

    single<MetadataProvider> {
        AndroidMetadataProvider(context = androidContext())
    }

    single<ImageRequestFactory> {
        AndroidImageRequestFactory(context = androidContext())
    }

}