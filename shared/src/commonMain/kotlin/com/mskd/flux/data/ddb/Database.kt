package com.mskd.flux.data.ddb

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.artwork.Season
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [Artwork::class, Movie::class, Episode::class, Season::class],
    version = 4,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(FluxDatabaseConstructor::class)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun dao(): DatabaseDao
}

@Suppress("KotlinNoActualForExpect")
expect object FluxDatabaseConstructor : RoomDatabaseConstructor<FluxDatabase> {
    override fun initialize(): FluxDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<FluxDatabase>
): FluxDatabase {
    return builder
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}