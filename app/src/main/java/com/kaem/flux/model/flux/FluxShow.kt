package com.kaem.flux.model.flux

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

@Entity
data class FluxShow(
    @PrimaryKey override val id: Int,
    @ColumnInfo override val title: String,
    @ColumnInfo override val imagePath: String,
    @ColumnInfo override val bannerPath: String,
    @ColumnInfo override val releaseDateString: String,
    @ColumnInfo var episodeIds: List<Int>
) : FluxArtworkSummary {

    @Transient
    @Ignore
    val releaseDate: Date? = releaseDateString.parseTMDBDate()

    constructor(tmdbArtwork: TMDBArtwork) : this(
        id = tmdbArtwork.id,
        title = tmdbArtwork.title,
        imagePath = tmdbArtwork.imagePath,
        bannerPath = tmdbArtwork.bannerPath,
        releaseDateString = tmdbArtwork.releaseDateString,
        episodeIds = emptyList()
    )

}