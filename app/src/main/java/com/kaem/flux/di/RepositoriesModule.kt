package com.kaem.flux.di

import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.repository.artwork.ArtworkRepository
import com.kaem.flux.data.repository.artwork.ArtworkRepositoryImpl
import com.kaem.flux.data.repository.catalog.CatalogRepository
import com.kaem.flux.data.repository.catalog.CatalogRepositoryImpl
import com.kaem.flux.data.source.file.FilesSource
import com.kaem.flux.data.source.media.MediaSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Provides
    @Singleton
    fun provideArtworkRepository(
        db: DatabaseDao
    ) : ArtworkRepository = ArtworkRepositoryImpl(
        db = db
    )

    @Provides
    @Singleton
    fun provideCatalogRepository(
        @FilesModule.LocalFilesDataSource localFilesSource: FilesSource,
        @MediaModule.MediaDataSourceLocal mediaSourceLocal: MediaSource,
        @MediaModule.MediaDataSourceTMDB mediaSourceTMDB: MediaSource,
        db: DatabaseDao
    ) : CatalogRepository = CatalogRepositoryImpl(
        fileSource = localFilesSource,
        mediaSourceLocal = mediaSourceLocal,
        mediaSourceTmdb = mediaSourceTMDB,
        db = db
    )

}