package com.mskd.flux.core.database.data.migrations

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
internal data class RoomSchemaFile(val database: RoomDatabaseSchema)

@Serializable
internal data class RoomDatabaseSchema(
    val version: Int,
    val entities: List<RoomEntitySchema>,
    val setupQueries: List<String> = emptyList()
)

@Serializable
internal data class RoomEntitySchema(
    val tableName: String,
    val createSql: String,
    val indices: List<RoomIndexSchema> = emptyList()
)

@Serializable
internal data class RoomIndexSchema(val createSql: String)

internal val json = Json { ignoreUnknownKeys = true }

internal fun loadSchemaSetupSql(resourcePath: String, expectedVersion: Int): List<String> {
    val jsonText = object {}.javaClass.classLoader
        ?.getResourceAsStream(resourcePath)
        ?.bufferedReader()
        ?.readText()
        ?: error("Schema not found on the classpath : $resourcePath. " +
                "Verify that 'schemaLocation' is correctly exposed as a test resource.")

    val schema = json
        .decodeFromString<RoomSchemaFile>(jsonText)
        .database

    check(schema.version == expectedVersion) {
        "The loaded schema is in version ${schema.version}, expected $expectedVersion." +
                "The file pointed to probably does not correspond to the correct migration."
    }

    // Fallback if the exported format does not contain setupQueries (older versions of Room)
    return schema.entities.flatMap { entity ->
        val create = entity.createSql.replace($$"${TABLE_NAME}", entity.tableName)
        val indices = entity.indices.map { it.createSql.replace($$"${TABLE_NAME}", entity.tableName) }
        listOf(create) + indices
    }
}