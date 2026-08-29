package com.mskd.flux.features.history.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.mskd.flux.core.database.data.model.MediaEntity
import com.mskd.flux.core.model.artwork.ContentType

@Entity(
    tableName = "history",
    primaryKeys = ["artworkId"],
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["id", "artworkId"],
            childColumns = ["mediaId", "artworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaId", "artworkId"])]
)
data class HistoryEntity(
    val artworkId: Long,
    val mediaId: Long,
    val timestamp: Long,
)
