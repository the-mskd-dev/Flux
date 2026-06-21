package com.mskd.flux.di

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.ddb.FluxDatabase
import com.mskd.flux.data.ddb.getRoomDatabase
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