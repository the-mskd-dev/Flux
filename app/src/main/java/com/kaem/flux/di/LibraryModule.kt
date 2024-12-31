package com.kaem.flux.di

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.repository.LibraryRepository
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.file.FilesDataSource
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
        @FilesModule.LocalFilesDataSource localFilesDataSource: FilesDataSource,
        @ArtworkModule.LocalArtworkDataSource localArtworkDataSource: ArtworkDataSource,
        @ArtworkModule.TMDBArtworkDataSource tmdbArtworkDataSource: ArtworkDataSource,
        db: FluxDao
    ) : LibraryRepository = LibraryRepository(
        fileSource = localFilesDataSource,
        localSource = localArtworkDataSource,
        tmdbSource = tmdbArtworkDataSource,
        db = db
    )

}