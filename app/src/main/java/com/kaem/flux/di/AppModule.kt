package com.kaem.flux.di

import android.content.Context
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.source.FilesDataSource
import com.kaem.flux.data.source.LocalFilesDataSource
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.home.HomeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Qualifier
    @Retention(AnnotationRetention.RUNTIME)
    annotation class LocalFilesDataSource

    @Provides
    @Singleton
    @LocalFilesDataSource
    fun provideLocalFilesDataSource(
        @ApplicationContext context: Context
    ) : FilesDataSource = LocalFilesDataSource(context)

}

@Module
@InstallIn(SingletonComponent::class)
object HomeRepositoryModule {

    @Provides
    @Singleton
    fun provideHomeRepository(
        @AppModule.LocalFilesDataSource localFilesDataSource: FilesDataSource,
        tmdbService: TMDBService,
        fluxDao: FluxDao
    ) : HomeRepository = HomeRepository(
        localFilesDataSource = localFilesDataSource,
        tmdbService = tmdbService,
        fluxDao = fluxDao
    )

}

