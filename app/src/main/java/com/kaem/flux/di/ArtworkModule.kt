package com.kaem.flux.di

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.source.artwork.ArtworkDataSource
import com.kaem.flux.data.source.artwork.ArtworkDataSourceDBImpl
import com.kaem.flux.data.source.artwork.ArtworkDataSourceTMDBImpl
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
    fun provideDatabaseArtworkDataSource(db: FluxDao) : ArtworkDataSource {
        return ArtworkDataSourceDBImpl(
            db = db
        )
    }

    @Provides
    @TMDBArtworkDataSource
    fun provideTMDBArtworkDataSource(
        tmdbService: TMDBService
    ) : ArtworkDataSource {
        return ArtworkDataSourceTMDBImpl(
            tmdbService = tmdbService
        )
    }

}