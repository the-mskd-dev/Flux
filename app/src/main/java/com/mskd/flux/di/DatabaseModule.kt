package com.mskd.flux.di

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.ddb.FluxDatabase
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val databaseModule = module {

    single<FluxDatabase> {
        FluxDatabase.getInstance(androidContext())
    }

    single<DatabaseDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.dao()
    }

    single<DatabaseRepositoryImpl>() bind DatabaseRepository::class

}