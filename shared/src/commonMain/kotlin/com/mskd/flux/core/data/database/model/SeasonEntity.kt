package com.mskd.flux.core.data.database.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "seasons",
    indices = [
        Index(value = ["artworkId"])
    ]
)
data class SeasonEntity(
    @PrimaryKey
    val id: Long,
    val artworkId: Long,
    val title: String,
    val description: String,
    val imagePath: String?,
    val season: Int
)
