package com.mskd.flux.core.database.data.migrations

import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import androidx.sqlite.execSQL
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.getRoomDatabase
import com.mskd.flux.core.database.data.mappers.toDomain
import com.mskd.flux.core.model.artwork.ContentType
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * Upgrade of a version 13 database, created from its exported schema, by the real Room database and its migrations.
 */
class Migration13To14Test : FunSpec({

    lateinit var file: File

    beforeTest { file = File.createTempFile("flux", ".db").apply { delete() } }
    afterTest { file.delete() }

    /** Tables and Room identity of the exported schema [version], with its user version. */
    fun createDatabase(version: Int, rows: List<String>) {
        val schema = javaClass.classLoader!!
            .getResource("com.mskd.flux.core.database.data.FluxDatabase/$version.json")!!
            .readText()
            .let { Json.parseToJsonElement(it).jsonObject["database"]!!.jsonObject }
        val entities = schema["entities"]!!.jsonArray.map { it.jsonObject }
        val queries = entities.flatMap { entity ->
            val table = entity["tableName"]!!.jsonPrimitive.content
            val indices = entity["indices"]?.jsonArray.orEmpty().map { it.jsonObject["createSql"]!!.jsonPrimitive.content }
            (listOf(entity["createSql"]!!.jsonPrimitive.content) + indices).map { it.replace("\${TABLE_NAME}", table) }
        } + schema["setupQueries"]!!.jsonArray.map { it.jsonPrimitive.content }

        BundledSQLiteDriver().open(file.absolutePath).use { connection ->
            (queries + rows).forEach { connection.execSQL(it) }
            connection.execSQL("PRAGMA user_version = $version")
        }
    }

    fun openDatabase(): FluxDatabase {
        val context = mockk<Context>(relaxed = true) { every { getDatabasePath(any()) } returns file }
        return getRoomDatabase(Room.databaseBuilder(context, FluxDatabase::class.java, file.name))
    }

    test("existing Artwork survive the upgrade untouched and are not manually identified") {

        // Given
        createDatabase(
            version = 13,
            rows = listOf(
                """
                INSERT INTO artworks (id, title, description, imagePath, bannerPath, type, lastModification, isPrivate, nsfw, genreIds, logoPath)
                VALUES (27205, 'Inception', 'A thief', '/poster.jpg', '/backdrop.jpg', 'MOVIE', 1000, 1, 0, '28,878', '/logo.png')
                """.trimIndent()
            )
        )

        // When
        val database = openDatabase()
        val artwork = database.artworkDao().getArtwork(artworkId = 27205)?.toDomain()
        database.close()

        // Then
        artwork?.title shouldBe "Inception"
        artwork?.type shouldBe ContentType.MOVIE
        artwork?.isPrivate shouldBe true
        artwork?.genreIds shouldBe listOf(28, 878)
        artwork?.logoPath shouldBe "/logo.png"
        artwork?.isManuallyIdentified shouldBe false

    }

})
