package com.mskd.flux.di

import android.content.Context
import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.source.media.MediaSource
import com.mskd.flux.data.source.media.MediaSourceDBImpl
import com.mskd.flux.data.source.media.MediaSourceTMDBImpl
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.data.tmdb.token.TokenProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
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
        tmdbService: TMDBService,
        tokenProvider: TokenProvider,
        @ApplicationContext context: Context
    ) : MediaSource {
        return MediaSourceTMDBImpl(tmdbService = tmdbService, tokenProvider = tokenProvider, context = context)
    }

}