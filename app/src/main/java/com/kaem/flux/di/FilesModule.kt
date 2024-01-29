package com.kaem.flux.di

import android.content.Context
import com.kaem.flux.data.source.file.FilesDataSource
import com.kaem.flux.data.source.file.LocalFilesDataSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object FilesModule {

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

