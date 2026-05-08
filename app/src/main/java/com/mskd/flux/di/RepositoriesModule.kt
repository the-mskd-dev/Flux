package com.mskd.flux.di

import android.content.Context
import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.artwork.ArtworkRepositoryImpl
import com.mskd.flux.data.repository.catalog.CatalogRepository
import com.mskd.flux.data.repository.catalog.CatalogRepositoryImpl
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepositoryImpl
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.files.FilesRepositoryImpl
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepositoryImpl
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.data.source.file.FilesSource
import com.mskd.flux.data.source.media.MediaSource
import com.mskd.flux.data.tmdb.TMDBService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoriesModule {

    @Provides
    @Singleton
    fun provideArtworkRepository(
        database: DatabaseRepository
    ) : ArtworkRepository = ArtworkRepositoryImpl(
        database = database
    )

    @Provides
    @Singleton
    fun provideCatalogRepository(
        @FilesModule.LocalFilesDataSource localFilesSource: FilesSource,
        @MediaModule.MediaDataSourceLocal mediaSourceLocal: MediaSource,
        @MediaModule.MediaDataSourceTMDB mediaSourceTMDB: MediaSource,
        database: DatabaseRepository,
        @CoroutineModule.ApplicationScope scope: CoroutineScope
    ) : CatalogRepository = CatalogRepositoryImpl(
        fileSource = localFilesSource,
        mediaSourceLocal = mediaSourceLocal,
        mediaSourceTmdb = mediaSourceTMDB,
        database = database,
        scope = scope
    )

    @Provides
    @Singleton
    fun provideDatabaseRepository(dao: DatabaseDao) : DatabaseRepository {
        return DatabaseRepositoryImpl(dao = dao)
    }

    @Provides
    @Singleton
    fun provideTmdbRepository(tmdbService: TMDBService) : TmdbRepository {
        return TmdbRepositoryImpl(tmdbService)
    }

    @Provides
    @Singleton
    fun provideFilesRepository(
        @ApplicationContext context: Context,
        userRepository: UserRepository
    ) : FilesRepository {
        return FilesRepositoryImpl(
            context = context,
            userRepository = userRepository
        )
    }

}