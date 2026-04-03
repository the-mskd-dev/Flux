package com.mskd.flux.di

import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.source.media.MediaSource
import com.mskd.flux.data.source.media.MediaSourceDBImpl
import com.mskd.flux.data.source.media.MediaSourceTMDBImpl
import com.mskd.flux.data.tmdb.TMDBService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object MediaModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MediaDataSourceLocal

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class MediaDataSourceTMDB

    @Provides
    @MediaDataSourceLocal
    fun provideMediaDataSourceLocal(db: DatabaseDao) : MediaSource {
        return MediaSourceDBImpl(
            db = db
        )
    }

    @Provides
    @MediaDataSourceTMDB
    fun provideMediaDataSourceTMDB(
        tmdbService: TMDBService
    ) : MediaSource {
        return MediaSourceTMDBImpl(tmdbService = tmdbService)
    }

}