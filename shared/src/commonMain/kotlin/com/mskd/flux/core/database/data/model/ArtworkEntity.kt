package com.mskd.flux.core.database.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mskd.flux.core.model.artwork.ContentType

@Entity(tableName = "artworks")
data class ArtworkEntity(
    @PrimaryKey
    val id: Long = 0,
    val title: String = "",
    @ColumnInfo(defaultValue = "")
    val description: String = "",
    val imagePath: String = "",
    val bannerPath: String = "",
    val type: ContentType = ContentType.SHOW,
    val lastModification: Long? = null,
    @ColumnInfo(defaultValue = "")
    val genreIds: List<Int> = emptyList()
)
