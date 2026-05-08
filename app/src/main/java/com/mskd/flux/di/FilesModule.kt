package com.mskd.flux.di

import android.content.Context
import com.mskd.flux.data.source.file.FilesSource
import com.mskd.flux.data.source.file.FilesSourceLocalImpl
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
    ) : FilesSource = FilesSourceLocalImpl(context)

}

