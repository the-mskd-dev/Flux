package com.mskd.flux.core.database.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.FileSource

@Entity(
    tableName = "medias",
    primaryKeys = ["id", "artworkId"],
    indices = [
        Index(value = ["artworkId"]),
        Index(value = ["path"], unique = true),
    ]
)
data class MediaEntity(
    val id: Long,
    val artworkId: Long,
    val type: ContentType,
    val title: String,
    val releaseDateString: String,
    val description: String,
    val voteAverage: Float,
    val voteCount: Int,
    val duration: Int,
    val currentTime: Long = 0L,
    val status: Status = Status.TO_WATCH,

    // Episode
    val number: Int? = null,
    val season: Int? = null,
    val imagePath: String? = null,

    // File
    @ColumnInfo(name = "name")
    val fileName: String,
    val addedDateTime: Long,
    val path: String,
    @ColumnInfo(defaultValue = "")
    val realPath: String,
    val source: FileSource,
    val parentDocId: String? = null
)