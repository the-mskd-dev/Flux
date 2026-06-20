package com.mskd.flux.di

import com.mskd.flux.shared.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.files.FilesRepositoryImpl
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val repositoriesModule = module {

    singleOf(::TmdbRepositoryImpl) bind TmdbRepository::class

}