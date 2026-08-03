package com.mskd.flux.core.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {

        // 1. Create new temporary table
        connection.execSQL(
            """
            CREATE TABLE media_new (
                id INTEGER NOT NULL,
                artworkId INTEGER NOT NULL,
                type TEXT NOT NULL,
                number INTEGER,
                season INTEGER,
                imagePath TEXT,
                title TEXT NOT NULL,
                releaseDateString TEXT NOT NULL,
                description TEXT NOT NULL,
                voteAverage REAL NOT NULL,
                voteCount INTEGER NOT NULL,
                duration INTEGER NOT NULL,
                currentTime INTEGER NOT NULL DEFAULT 0,
                status TEXT NOT NULL DEFAULT 'TO_WATCH',
                name TEXT NOT NULL,
                addedDateTime INTEGER NOT NULL,
                path TEXT NOT NULL,
                realPath TEXT NOT NULL DEFAULT '',
                source TEXT NOT NULL,
                parentDocId TEXT,
                PRIMARY KEY (id, artworkId)
            )
            """.trimIndent()
        )

        // 2. Import all episodes
        connection.execSQL(
            """
            INSERT INTO media_new
            (id, artworkId, type, number, season, imagePath, title, releaseDateString,
             description, voteAverage, voteCount, duration, currentTime, status,
             name, addedDateTime, path, realPath, source, parentDocId)
            SELECT id, artworkId, 'SHOW', number, season, imagePath, title, releaseDateString,
                   description, voteAverage, voteCount, duration, currentTime, status,
                   name, addedDateTime, path, '', source, parentDocId
            FROM episodes
            """.trimIndent()
        )

        // 3. Import all movies
        connection.execSQL(
            """
            INSERT INTO media_new
            (id, artworkId, type, number, season, imagePath, title, releaseDateString,
             description, voteAverage, voteCount, duration, currentTime, status,
             name, addedDateTime, path, realPath, source, parentDocId)
            SELECT artworkId, artworkId, 'MOVIE', NULL, NULL, NULL, title, releaseDateString,
                   description, voteAverage, voteCount, duration, currentTime, status,
                   name, addedDateTime, path, '', source, parentDocId
            FROM movies
            """.trimIndent()
        )

        // 4. Delete duplicates
        connection.execSQL(
            """
            DELETE FROM media_new
            WHERE rowid NOT IN (
                SELECT rowid FROM (
                    SELECT rowid,
                           ROW_NUMBER() OVER (
                               PARTITION BY path
                               ORDER BY
                                   CASE WHEN currentTime != 0 OR status != 'TO_WATCH' THEN 0 ELSE 1 END,
                                   addedDateTime DESC
                           ) AS rn
                    FROM media_new
                )
                WHERE rn = 1
            )
            """.trimIndent()
        )

        // Clean old tables
        connection.execSQL("DROP TABLE episodes")
        connection.execSQL("DROP TABLE movies")
        connection.execSQL("ALTER TABLE media_new RENAME TO medias")

        // Create indexes
        connection.execSQL("CREATE INDEX index_media_artworkId ON medias(artworkId)")
        connection.execSQL("CREATE UNIQUE INDEX index_media_path ON medias(path)")
    }
}