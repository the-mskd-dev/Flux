package com.mskd.flux.core.database

import com.mskd.flux.core.database.data.DatabaseDao
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.getRoomDatabase
import com.mskd.flux.core.database.data.repository.DatabaseRepositoryImpl
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
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