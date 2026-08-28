package com.mskd.flux.features.history.data.model

import androidx.room.Entity
import com.mskd.flux.core.model.artwork.ContentType

@Entity(
    tableName = "history",
    primaryKeys = ["id", "artworkId"],
)
data class HistoryEntity(
    val id: Long,
    val artworkId: Long,
    val type: ContentType,
    val title: String,
    val season: Int?,
    val number: Int?,
    val path: String,
    val duration: Int,
    val currentTime: Long,
    val timestamp: Long,
)
