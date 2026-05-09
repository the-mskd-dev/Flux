package com.mskd.flux.di

import com.mskd.flux.data.repository.artwork.ArtworkRepository
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.artwork.ArtworkUCImpl
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
    fun provideCatalogUC(
        tmdbRepository: TmdbRepository,
        databaseRepository: DatabaseRepository,
        filesRepository: FilesRepository,
        userRepository: UserRepository
    ) : CatalogUC {
        return CatalogUCImpl(
            tmdb = tmdbRepository,
            database = databaseRepository,
            files = filesRepository,
            user = userRepository
        )
    }

    @Provides
    @Singleton
    fun provideArtworkUC(
        databaseRepository: DatabaseRepository,
    ) : ArtworkUC {
        return ArtworkUCImpl(
            database = databaseRepository,
        )
    }

    @Provides
    @Singleton
    fun provideProgressUC(
        databaseRepository: DatabaseRepository,
        userRepository: UserRepository
    ) : ProgressUC {
        return ProgressUCImpl(
            database = databaseRepository,
            user = userRepository
        )
    }

}