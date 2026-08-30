package com.mskd.flux.core.database

import com.mskd.flux.core.database.data.dao.ArtworkDao
import com.mskd.flux.core.database.data.dao.DetailsDao
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.dao.MediasDao
import com.mskd.flux.core.database.data.dao.SeasonsDao
import com.mskd.flux.core.database.data.getRoomDatabase
import com.mskd.flux.core.database.data.repository.DatabaseRepositoryImpl
import com.mskd.flux.core.database.data.repository.DetailsRepositoryImpl
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import org.koin.dsl.module

val moduleDatabase = module {

    single<FluxDatabase> { getRoomDatabase(builder = get()) }

    single<ArtworkDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.artworkDao()
    }

    single<MediasDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.mediasDao()
    }

    single<SeasonsDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.seasonsDao()
    }

    single<DetailsDao> {
        val fluxDatabase = get<FluxDatabase>()
        fluxDatabase.detailsDao()
    }

    single<DatabaseRepository> {
        DatabaseRepositoryImpl(
            artworksDao = get(),
            mediasDao = get(),
            seasonsDao = get()
        )
    }

    single<DetailsRepository> {
        DetailsRepositoryImpl(dao = get())
    }

}