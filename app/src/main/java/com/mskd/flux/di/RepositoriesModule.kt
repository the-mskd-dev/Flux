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
import org.koin.dsl.module

val repositoriesModule = module {

    single<DatabaseRepository> {
        DatabaseRepositoryImpl(dao = get())
    }

    single<TmdbRepository> {
        TmdbRepositoryImpl(
            tmdbService = get(),
            settings = get()
        )
    }

    single<FilesRepository> {
        FilesRepositoryImpl(
            context = androidContext(),
            userRepository = get()
        )
    }

    single<ConnectivityRepository> {
        ConnectivityRepositoryImpl(context = androidContext())
    }

}