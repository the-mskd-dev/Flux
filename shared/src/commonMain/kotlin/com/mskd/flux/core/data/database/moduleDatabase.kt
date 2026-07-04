package com.mskd.flux.core.data.database

import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import org.koin.dsl.module

val moduleDatabase = module {

    single<FluxDatabase> { getRoomDatabase(builder = get()) }

    single<DatabaseRepository> {
        DatabaseRepositoryImpl(dao = get())
    }

}