package com.mskd.flux.core.database.data.migrations

import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

val MIGRATION_5_6 = object : Migration(5, 6) {
    override fun migrate(connection: SQLiteConnection) {

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

        connection.execSQL("DROP TABLE episodes")
        connection.execSQL("DROP TABLE movies")
        connection.execSQL("ALTER TABLE media_new RENAME TO medias")

        connection.execSQL("CREATE INDEX index_media_artworkId ON medias(artworkId)")
        connection.execSQL("CREATE UNIQUE INDEX index_media_path ON medias(path)")
    }
}