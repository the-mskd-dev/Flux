package com.mskd.flux.di

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.ddb.FluxDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    single<FluxDatabase> {
        FluxDatabase.getInstance(androidContext())
    }

    single<DatabaseDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.dao()
    }

}