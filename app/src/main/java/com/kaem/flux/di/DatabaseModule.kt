package com.kaem.flux.di

import android.content.Context
import androidx.room.Room
import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.data.ddb.FluxDatabase
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

        return Room.databaseBuilder(
            context = context,
            klass = FluxDatabase::class.java,
            name = "fluxDatabase"
        ).build()

    }

    @Provides
    fun provideDao(fluxDatabase: FluxDatabase) : FluxDao {
        return fluxDatabase.fluxDao()
    }

}