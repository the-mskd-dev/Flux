package com.kaem.flux.di

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object LibraryModule {

    @Provides
    @Singleton
    fun provideLibraryRepository(
        @AppModule.LocalFilesDataSource localFilesDataSource: FilesDataSource,
        tmdbService: TMDBService,
        databaseManager: DatabaseManager
    ) : LibraryRepository = LibraryRepository(
        localFilesDataSource = localFilesDataSource,
        tmdbService = tmdbService,
        databaseManager = databaseManager
    )

}