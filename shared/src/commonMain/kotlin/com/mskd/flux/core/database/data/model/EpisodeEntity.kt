package com.mskd.flux.core.database.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.mskd.flux.core.model.artwork.Status
import com.mskd.flux.core.model.files.FileSource

@Entity(
    tableName = "episodes",
    indices = [
        Index(value = ["artworkId"])
    ]
)
data class EpisodeEntity(
    @PrimaryKey
    val id: Long,
    val number: Int,
    val season: Int,
    val imagePath: String,
    val artworkId: Long,
    val title: String,
    val releaseDateString: String,
    val description: String,
    val duration: Int,
    val currentTime: Long = 0L,
    val voteAverage: Float,
    val voteCount: Int,
    val status: Status = Status.TO_WATCH,

    // File
    @ColumnInfo(name = "name")
    val fileName: String,
    val addedDateTime: Long,
    val path: String,
    val source: FileSource,
    val parentDocId: String? = null
)