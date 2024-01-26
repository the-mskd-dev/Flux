package com.kaem.flux.model.flux

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.tmdb.TMDBMovie
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

data class FluxMovie(
    override val id: Int,
    override val title: String,
    override val imagePath: String,
    override val bannerPath: String,
    override val releaseDateString: String,
    override val description: String,
    override val voteAverage: Float,
    override val voteCount: Int,
    override val duration: Int,
    override var isWatched: Boolean = false,
    override val file: FileSource,
    val genres: List<String> = listOf(),
) : FluxArtworkSummary, FluxArtwork {

    @Transient
    val releaseDate: Date? = releaseDateString.parseTMDBDate()

    constructor(
        tmdbMovie: TMDBMovie,
        file: FileSource
    ) : this(
        id = tmdbMovie.id,
        title = tmdbMovie.title,
        imagePath = tmdbMovie.imagePath,
        bannerPath = tmdbMovie.bannerPath,
        releaseDateString = tmdbMovie.releaseDateString,
        description = tmdbMovie.description,
        voteAverage = tmdbMovie.voteAverage,
        voteCount = tmdbMovie.voteCount,
        duration = tmdbMovie.duration,
        file = file,
        genres = emptyList()
    )
}
