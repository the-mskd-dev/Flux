package com.mskd.flux.data.ddb

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

fun getDatabaseBuilder(context: Context): RoomDatabase.Builder<FluxDatabase> {
    val appContext = context.applicationContext
    val dbFile = appContext.getDatabasePath("fluxDatabase")
    return Room.databaseBuilder<FluxDatabase>(
        context = appContext,
        name = dbFile.absolutePath
    )
}