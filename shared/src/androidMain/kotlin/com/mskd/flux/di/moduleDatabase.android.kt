package com.mskd.flux.di

import androidx.room.RoomDatabase
import com.mskd.flux.data.ddb.FluxDatabase
import com.mskd.flux.data.ddb.getDatabaseBuilder
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleDatabaseAndroid = module {

    single<RoomDatabase.Builder<FluxDatabase>> {
        getDatabaseBuilder(androidContext())
    }

}