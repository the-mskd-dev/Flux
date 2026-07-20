package com.mskd.flux.core.database

import androidx.room.RoomDatabase
import com.mskd.flux.core.database.data.FluxDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val moduleDatabaseAndroid = module {

    single<RoomDatabase.Builder<FluxDatabase>> {
        getDatabaseBuilder(androidContext())
    }

}