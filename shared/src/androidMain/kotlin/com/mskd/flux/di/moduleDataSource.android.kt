package com.mskd.flux.di

import com.mskd.flux.data.repository.sources.AndroidCheckFolderAvailabilityDataSource
import com.mskd.flux.data.repository.sources.CheckFolderAvailabilityDataSource
import org.koin.dsl.module

val moduleDataSourceAndroid = module {

    single<CheckFolderAvailabilityDataSource> {
        AndroidCheckFolderAvailabilityDataSource(
            context = get()
        )
    }

}