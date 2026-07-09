package com.mskd.flux.core.database.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.FileSource

@Entity(
    tableName = "movies",
    indices = [
        Index(value = ["artworkId"])
    ]
)
data class MovieEntity(
    @PrimaryKey
    val artworkId: Long,
    val title: String,
    val releaseDateString: String,
    val description: String,
    val voteAverage: Float,
    val voteCount: Int,
    val duration: Int,
    val currentTime: Long = 0L,
    val status: Status = Status.TO_WATCH,

    // File
    @ColumnInfo(name = "name")
    val fileName: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource,
    val parentDocId: String? = null
)