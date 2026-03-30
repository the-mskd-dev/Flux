package com.mskd.flux.di

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.artwork.ArtworkRepositoryImpl
import com.mskd.flux.data.repository.catalog.CatalogRepository
import com.mskd.flux.data.repository.catalog.CatalogRepositoryImpl
import com.mskd.flux.data.source.file.FilesSource
import com.mskd.flux.data.source.media.MediaSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
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
        db: DatabaseDao,
        @CoroutineModule.ApplicationScope scope: CoroutineScope
    ) : CatalogRepository = CatalogRepositoryImpl(
        fileSource = localFilesSource,
        mediaSourceLocal = mediaSourceLocal,
        mediaSourceTmdb = mediaSourceTMDB,
        db = db,
        scope = scope
    )

}