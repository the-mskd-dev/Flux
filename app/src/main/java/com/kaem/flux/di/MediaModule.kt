package com.kaem.flux.di

import com.google.firebase.analytics.FirebaseAnalytics
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.source.media.MediaDataSource
import com.kaem.flux.data.source.media.MediaDataSourceDBImpl
import com.kaem.flux.data.source.media.MediaDataSourceTMDBImpl
import com.kaem.flux.data.tmdb.TMDBService
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
    annotation class LocalMediaDataSource

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class TMDBMediaDataSource

    @Provides
    @LocalMediaDataSource
    fun provideDatabaseMediaDataSource(db: FluxDao) : MediaDataSource {
        return MediaDataSourceDBImpl(
            db = db
        )
    }

    @Provides
    @TMDBMediaDataSource
    fun provideTMDBMediaDataSource(
        tmdbService: TMDBService,
        firebaseAnalytics: FirebaseAnalytics
    ) : MediaDataSource {
        return MediaDataSourceTMDBImpl(
            tmdbService = tmdbService,
            firebaseAnalytics = firebaseAnalytics
        )
    }

}