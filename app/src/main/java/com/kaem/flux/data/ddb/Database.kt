package com.kaem.flux.data.ddb

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity
data class ArtworkEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val content: String
)

@Entity
data class EpisodeEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val showId: Int,
    @ColumnInfo val content: String
)