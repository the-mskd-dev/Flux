package com.kaem.flux.di

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.artwork.DatabaseArtworkDataSource
import com.kaem.flux.data.source.artwork.TMDBArtworkDataSource
import com.kaem.flux.data.tmdb.TMDBService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object ArtworkModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalArtworkDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TMDBArtworkDataSource

    @Provides
    @LocalArtworkDataSource
    fun provideDatabaseArtworkDataSource(databaseManager: DatabaseManager) : ArtworkDataSource {
        return DatabaseArtworkDataSource(
            databaseManager = databaseManager
        )
    }

    @Provides
    @TMDBArtworkDataSource
    fun provideTMDBArtworkDataSource(
        databaseManager: DatabaseManager,
        tmdbService: TMDBService
    ) : ArtworkDataSource {
        return TMDBArtworkDataSource(
            databaseManager = databaseManager,
            tmdbService = tmdbService
        )
    }

}