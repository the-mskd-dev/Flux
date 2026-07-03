package com.mskd.flux.di

import com.mskd.flux.data.local.ddb.DatabaseDao
import com.mskd.flux.data.local.ddb.FluxDatabase
import com.mskd.flux.data.local.ddb.SourcesDao
import com.mskd.flux.data.local.ddb.getRoomDatabase
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import com.mskd.flux.data.repository.ddb.sources.SourcesRepository
import com.mskd.flux.data.repository.ddb.sources.SourcesRepositoryImpl
import org.koin.dsl.module

val moduleDatabase = module {

    single<FluxDatabase> { getRoomDatabase(builder = get()) }

    single<DatabaseDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.dao()
    }

    single<SourcesDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.sourcesDao()
    }

    single<DatabaseRepository> {
        DatabaseRepositoryImpl(dao = get())
    }

    single<SourcesRepository> {
        SourcesRepositoryImpl(dao = get())
    }

}