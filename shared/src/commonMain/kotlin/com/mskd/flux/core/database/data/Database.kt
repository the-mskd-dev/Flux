package com.mskd.flux.core.database.data

import androidx.room.AutoMigration
import androidx.room.ConstructedBy
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import androidx.room.TypeConverters
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.mskd.flux.core.database.data.migrations.MIGRATION_5_6
import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.database.data.model.GenreEntity
import com.mskd.flux.core.database.data.model.MediaEntity
import com.mskd.flux.core.database.data.model.SeasonEntity
import com.mskd.flux.features.sources.data.local.SourcesDao
import com.mskd.flux.features.sources.data.local.UserFolderEntity
import kotlinx.coroutines.Dispatchers

@Database(
    entities = [
        ArtworkEntity::class,
        SeasonEntity::class,
        MediaEntity::class,
        UserFolderEntity::class,
        GenreEntity::class
    ],
    version = 8,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3),
        AutoMigration(from = 3, to = 4),
        AutoMigration(from = 4, to = 5),
        AutoMigration(from = 6, to = 7),
        AutoMigration(from = 7, to = 8),
    ]
)
@TypeConverters(Converters::class)
@ConstructedBy(FluxDatabaseConstructor::class)
abstract class FluxDatabase : RoomDatabase() {
    abstract fun dao(): DatabaseDao
    abstract fun sourcesDao(): SourcesDao
    abstract fun detailsDao(): DetailsDao
}

expect object FluxDatabaseConstructor : RoomDatabaseConstructor<FluxDatabase> {
    override fun initialize(): FluxDatabase
}

fun getRoomDatabase(
    builder: RoomDatabase.Builder<FluxDatabase>
): FluxDatabase {
    return builder
        .addMigrations(MIGRATION_5_6)
        .setDriver(BundledSQLiteDriver())
        .setQueryCoroutineContext(Dispatchers.IO)
        .build()
}