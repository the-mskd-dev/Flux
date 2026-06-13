package com.mskd.flux.di

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.ddb.FluxDatabase
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val databaseModule = module {

    single<FluxDatabase> {
        FluxDatabase.getInstance(androidContext())
    }

    single<DatabaseDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.dao()
    }

    singleOf(::DatabaseRepositoryImpl) bind DatabaseRepository::class

}