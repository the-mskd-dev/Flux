package com.mskd.flux.di

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.useCases.progress.ProgressUCImpl
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.catalog.CatalogUCImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCasesModule {

    @Provides
    @Singleton
    fun provideArtworkProgressUC(
        artworkRepository: ArtworkRepository,
        userRepository: UserRepository
    ) : ProgressUC {
        return ProgressUCImpl(
            artworkRepository = artworkRepository,
            userRepository = userRepository
        )
    }

    @Provides
    @Singleton
    fun provideCatalogUC(
        tmdbRepository: TmdbRepository,
        databaseRepository: DatabaseRepository,
        filesRepository: FilesRepository,
        userRepository: UserRepository
    ) : CatalogUC {
        return CatalogUCImpl(
            tmdbRepository = tmdbRepository,
            databaseRepository = databaseRepository,
            filesRepository = filesRepository,
            userRepository = userRepository
        )
    }

}