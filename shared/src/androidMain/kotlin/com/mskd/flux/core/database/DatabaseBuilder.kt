package com.mskd.flux.core.database

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mskd.flux.core.database.data.FluxDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<FluxDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("fluxDatabase")
    return Room.databaseBuilder<FluxDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}