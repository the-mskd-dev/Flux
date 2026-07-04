package com.mskd.flux.core.database

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.model.data.local.entities.ArtworkEntity
import com.mskd.flux.model.data.local.entities.EpisodeEntity
import com.mskd.flux.model.data.local.entities.MovieEntity
import com.mskd.flux.model.data.local.entities.SeasonEntity
import com.mskd.flux.features.sources.data.local.UserFolderEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ArtworkEntity::class,
        MovieEntity::class,
        EpisodeEntity::class,
        SeasonEntity::class,
        UserFolderEntity::class
    ],
    version = 5,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(FluxDatabaseConstructor::class)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun dao(): DatabaseDao
    abstract fun sourcesDao(): SourcesDao
}

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