package com.mskd.flux.di

import com.mskd.flux.data.dataSources.AndroidCheckFolderAvailabilityDataSource
import com.mskd.flux.data.dataSources.CheckFolderAvailabilityDataSource
import org.koin.dsl.module

val moduleDataSourceAndroid = module {

    single<CheckFolderAvailabilityDataSource> {
        AndroidCheckFolderAvailabilityDataSource(
            context = get()
        )
    }

}