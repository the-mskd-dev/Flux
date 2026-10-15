package com.mskd.flux.features.people.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.mskd.flux.core.database.data.model.ArtworkEntity
import com.mskd.flux.core.model.artwork.PersonType

@Entity(
    tableName = "person",
    primaryKeys = ["artworkId", "creditId"],
    foreignKeys = [
        ForeignKey(
            entity = ArtworkEntity::class,
            parentColumns = ["id"],
            childColumns = ["artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["artworkId"])]
)
data class PersonEntity(
    val artworkId: Long,
    val id: Int,
    val creditId: String,
    val name: String,
    val originalName: String?,
    val profilePath: String?,
    val role: String,
    val type: PersonType,
    @ColumnInfo(name = "personOrder")
    val personOrder: Int = 0,
)