package com.kaem.flux.model.flux

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

data class FluxShow(
    override val id: Int,
    override val title: String,
    override val imagePath: String,
    override val bannerPath: String,
    override val releaseDateString: String
) : FluxArtworkSummary {

    @Transient
    val releaseDate: Date? = releaseDateString.parseTMDBDate()

    constructor(tmdbArtwork: TMDBArtwork) : this(
        id = tmdbArtwork.id,
        title = tmdbArtwork.title,
        imagePath = tmdbArtwork.imagePath,
        bannerPath = tmdbArtwork.bannerPath,
        releaseDateString = tmdbArtwork.releaseDateString
    )

}