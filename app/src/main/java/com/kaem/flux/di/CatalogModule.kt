package com.kaem.flux.di

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.repository.CatalogRepository
import com.kaem.flux.data.source.media.MediaSource
import com.kaem.flux.data.source.file.FilesSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CatalogModule {

    @Provides
    @Singleton
    fun provideLibraryRepository(
        @FilesModule.LocalFilesDataSource localFilesSource: FilesSource,
        @MediaModule.MediaDataSourceLocal mediaSourceLocal: MediaSource,
        @MediaModule.MediaDataSourceTMDB mediaSourceTMDB: MediaSource,
        db: FluxDao
    ) : CatalogRepository = CatalogRepository(
        fileSource = localFilesSource,
        mediaSourceLocal = mediaSourceLocal,
        mediaSourceTmdb = mediaSourceTMDB,
        db = db
    )

}