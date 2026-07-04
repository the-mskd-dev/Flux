package com.mskd.flux.core.data.database

import com.mskd.flux.core.data.database.repository.DatabaseRepository
import com.mskd.flux.core.data.database.repository.DatabaseRepositoryImpl
import org.koin.dsl.module

val moduleDatabase = module {

    single<FluxDatabase> { getRoomDatabase(builder = get()) }

    single<DatabaseRepository> {
        DatabaseRepositoryImpl(dao = get())
    }

    single<DatabaseDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.dao()
    }

}