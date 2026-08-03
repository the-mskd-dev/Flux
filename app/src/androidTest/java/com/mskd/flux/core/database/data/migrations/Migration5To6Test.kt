package com.mskd.flux.core.database.data.migrations

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Migration5To6Test : FunSpec({

    fun SQLiteConnection.createV5Schema() {
        loadSchemaSetupSql(
            resourcePath = "com.mskd.flux.core.database.data.FluxDatabase/5.json",
            expectedVersion = 5
        ).forEach { execSQL(it) }
    }

    fun SQLiteConnection.insertEpisode(
        id: Long, artworkId: Long, path: String,
        currentTime: Long = 0, status: String = "TO_WATCH"
    ) {
        execSQL("""
            INSERT INTO episodes 
            (id, artworkId, number, season, title, releaseDateString, description,
             voteAverage, voteCount, duration, currentTime, status, name, addedDateTime, path, source)
            VALUES ($id, $artworkId, 1, 1, 'Ep', '2024-01-01', 'desc',
             0.0, 0, 1200, $currentTime, '$status', 'file.mkv', 1000, '$path', 'MEDIA_STORE')
        """.trimIndent())
    }

    fun SQLiteConnection.insertMovie(
        artworkId: Long, path: String,
        currentTime: Long = 0, status: String = "TO_WATCH"
    ) {
        execSQL("""
            INSERT INTO movies
            (artworkId, title, releaseDateString, description, voteAverage, voteCount,
             duration, currentTime, status, name, addedDateTime, path, source)
            VALUES ($artworkId, 'Movie', '2024-01-01', 'desc', 0.0, 0,
             6000, $currentTime, '$status', 'movie.mkv', 1000, '$path', 'MEDIA_STORE')
        """.trimIndent())
    }

    test("Deduplicate the conflicting math within episodes before creating the unique index") {
        val connection = BundledSQLiteDriver().open(":memory:")
        connection.createV5Schema()

        connection.insertEpisode(id = 1, artworkId = 10, path = "/dup.mkv")
        connection.insertEpisode(id = 2, artworkId = 10, path = "/dup.mkv")

        MIGRATION_5_6.migrate(connection)

        val count = connection.prepare("SELECT COUNT(*) FROM medias WHERE path = ?").use {
            it.bindText(1, "/dup.mkv"); it.step(); it.getLong(0)
        }
        count shouldBe 1

        connection.close()
    }

    test("In case of a cross-table duplicate, the line with the viewing progress wins.") {
        val connection = BundledSQLiteDriver().open(":memory:")
        connection.createV5Schema()

        connection.insertEpisode(id = 1, artworkId = 10, path = "/dup.mkv", currentTime = 0, status = "TO_WATCH")
        connection.insertMovie(artworkId = 20, path = "/dup.mkv", currentTime = 4200, status = "WATCHING")

        MIGRATION_5_6.migrate(connection)

        val survivor = connection.prepare("SELECT type, currentTime FROM medias WHERE path = ?").use {
            it.bindText(1, "/dup.mkv"); it.step()
            it.getText(0) to it.getLong(1)
        }
        survivor shouldBe ("MOVIE" to 4200L)

        connection.close()
    }
})