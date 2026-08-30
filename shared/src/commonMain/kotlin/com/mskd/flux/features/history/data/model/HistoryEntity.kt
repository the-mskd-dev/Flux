package com.mskd.flux.features.history.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import com.mskd.flux.core.database.data.model.MediaEntity

@Entity(
    tableName = "history",
    primaryKeys = ["historyArtworkId"],
    foreignKeys = [
        ForeignKey(
            entity = MediaEntity::class,
            parentColumns = ["id", "artworkId"],
            childColumns = ["mediaId", "historyArtworkId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaId", "historyArtworkId"])]
)
data class HistoryEntity(
    @ColumnInfo(name = "historyArtworkId")
    val artworkId: Long,
    val mediaId: Long,
    val timestamp: Long,
)
