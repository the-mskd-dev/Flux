package com.mskd.flux.core.database.data.migrations

import androidx.sqlite.SQLiteConnection
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
@MediumTest
class Migration5To6Test {

    private fun SQLiteConnection.createV5Schema() {
        loadSchemaSetupSql(
            resourcePath = "com.mskd.flux.core.database.data.FluxDatabase/5.json",
            expectedVersion = 5
        ).forEach { execSQL(it) }
    }

    private fun SQLiteConnection.insertEpisode(
        id: Long, artworkId: Long, path: String,
        currentTime: Long = 0, status: String = "TO_WATCH"
    ) {
        execSQL("""
            INSERT INTO episodes 
            (id, artworkId, number, season, imagePath, title, releaseDateString, description,
             voteAverage, voteCount, duration, currentTime, status, name, addedDateTime, path, source)
            VALUES ($id, $artworkId, 1, 1, 'imagePath', 'Ep', '2024-01-01', 'desc',
             0.0, 0, 1200, $currentTime, '$status', 'file.mkv', 1000, '$path', 'MEDIA_STORE')
        """.trimIndent())
    }

    private fun SQLiteConnection.insertMovie(
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

    @Test
    fun resolveConflictingPathBeforeCreatingTheUniqueIndex() {
        val connection = BundledSQLiteDriver().open(":memory:")
        connection.createV5Schema()

        connection.insertEpisode(id = 1, artworkId = 10, path = "/dup.mkv")
        connection.insertEpisode(id = 2, artworkId = 10, path = "/dup.mkv")

        MIGRATION_5_6.migrate(connection)

        val count = connection.prepare("SELECT COUNT(*) FROM medias WHERE path = ?").use {
            it.bindText(1, "/dup.mkv"); it.step(); it.getLong(0)
        }
        assertEquals(1, count)

        connection.close()
    }

    @Test
    fun prioritizeProgressInCaseOfConflictingPath() {
        val connection = BundledSQLiteDriver().open(":memory:")
        connection.createV5Schema()

        connection.insertEpisode(id = 1, artworkId = 10, path = "/dup.mkv", currentTime = 0, status = "TO_WATCH")
        connection.insertMovie(artworkId = 20, path = "/dup.mkv", currentTime = 4200, status = "WATCHING")

        MIGRATION_5_6.migrate(connection)

        val survivor = connection.prepare("SELECT type, currentTime FROM medias WHERE path = ?").use {
            it.bindText(1, "/dup.mkv"); it.step()
            it.getText(0) to it.getLong(1)
        }

        assertEquals(("MOVIE" to 4200L), survivor)

        connection.close()
    }
}