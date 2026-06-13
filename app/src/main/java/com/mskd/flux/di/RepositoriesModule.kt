package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.files.FilesRepositoryImpl
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module
import org.koin.plugin.module.dsl.single

val repositoriesModule = module {

    single<DatabaseRepositoryImpl>() bind DatabaseRepository::class
    single<TmdbRepositoryImpl>() bind TmdbRepository::class
    single<FilesRepositoryImpl>() bind FilesRepository::class
    single<ConnectivityRepositoryImpl>() bind ConnectivityRepository::class

}