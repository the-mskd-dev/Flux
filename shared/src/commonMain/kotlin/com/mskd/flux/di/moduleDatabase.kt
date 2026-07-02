package com.mskd.flux.di

import com.mskd.flux.data.local.ddb.DatabaseDao
import com.mskd.flux.data.local.ddb.FluxDatabase
import com.mskd.flux.data.local.ddb.getRoomDatabase
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import org.koin.dsl.module

val moduleDatabase = module {

    single<FluxDatabase> { getRoomDatabase(builder = get()) }

    single<DatabaseDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.dao()
    }

    single<DatabaseRepository> {
        DatabaseRepositoryImpl(dao = get())
    }

}