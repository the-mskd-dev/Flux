package com.mskd.flux.di

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.useCases.artworkProgress.ArtworkProgressUC
import com.mskd.flux.useCases.artworkProgress.ArtworkProgressUCImpl
import com.mskd.flux.useCases.catalogUC.CatalogUC
import com.mskd.flux.useCases.catalogUC.CatalogUCImpl
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
    ) : ArtworkProgressUC {
        return ArtworkProgressUCImpl(
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