package com.mskd.flux.di

import android.content.Context
import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.ddb.FluxDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ) : FluxDatabase {
        return FluxDatabase.getInstance(context)
    }

    @Provides
    @Singleton
    fun provideDao(fluxDatabase: FluxDatabase) : DatabaseDao {
        return fluxDatabase.dao()
    }


}