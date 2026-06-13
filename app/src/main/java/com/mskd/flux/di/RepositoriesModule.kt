package com.mskd.flux.di

import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import com.mskd.flux.data.repository.connectivity.ConnectivityRepositoryImpl
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.files.FilesRepositoryImpl
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoriesModule = module {

    singleOf(::DatabaseRepositoryImpl) bind DatabaseRepository::class
    singleOf(::TmdbRepositoryImpl) bind TmdbRepository::class
    singleOf(::FilesRepositoryImpl) bind FilesRepository::class
    singleOf(::ConnectivityRepositoryImpl) bind ConnectivityRepository::class

}