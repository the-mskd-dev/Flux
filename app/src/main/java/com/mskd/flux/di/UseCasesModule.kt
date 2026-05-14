package com.mskd.flux.di

import android.content.Context
import coil3.ImageLoader
import com.mskd.flux.data.repository.ddb.DatabaseRepository
import com.mskd.flux.data.repository.files.FilesRepository
import com.mskd.flux.data.repository.settings.SettingsRepository
import com.mskd.flux.data.repository.tmdb.TmdbRepository
import com.mskd.flux.data.repository.user.UserRepository
import com.mskd.flux.useCases.artwork.ArtworkUC
import com.mskd.flux.useCases.artwork.ArtworkUCImpl
import com.mskd.flux.useCases.catalog.CatalogUC
import com.mskd.flux.useCases.catalog.CatalogUCImpl
import com.mskd.flux.useCases.images.ImagesUC
import com.mskd.flux.useCases.images.ImagesUCImpl
import com.mskd.flux.useCases.progress.ProgressUC
import com.mskd.flux.useCases.progress.ProgressUCImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
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
        userRepository: UserRepository,
        settingsRepository: SettingsRepository,
        @CoroutineModule.ApplicationScope scope: CoroutineScope,
        @ApplicationContext context: Context
    ) : CatalogUC {
        return CatalogUCImpl(
            tmdb = tmdbRepository,
            database = databaseRepository,
            files = filesRepository,
            user = userRepository,
            settings = settingsRepository,
            scope = scope,
            context = context
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

    @Provides
    @Singleton
    fun provideImagesUC(
        imageLoader: ImageLoader,
        @ApplicationContext context: Context
    ) : ImagesUC {
        return ImagesUCImpl(
            imageLoader = imageLoader,
            context = context
        )
    }

}