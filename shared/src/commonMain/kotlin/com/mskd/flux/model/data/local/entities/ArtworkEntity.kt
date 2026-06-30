package com.mskd.flux.model.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mskd.flux.model.domain.artwork.ContentType

@Entity(tableName = "artworks")
data class ArtworkEntity(
    @PrimaryKey
    val id: Long = 0,
    val title: String = "",
    @ColumnInfo(defaultValue = "")
    val description: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW
)
