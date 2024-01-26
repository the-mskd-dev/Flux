package com.kaem.flux.model.flux

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.tmdb.TMDBCrew
import com.kaem.flux.model.tmdb.TMDBEpisode
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

data class FluxEpisode(
    val id: Int,
    val showId: Int,
    val title: String,
    val number: Int,
    val season: Int,
    val imagePath: String,
    val releaseDateString: String,
    val crew: List<TMDBCrew>,
    override val description: String,
    override val duration: Int,
    override val voteAverage: Float,
    override val voteCount: Int,
    override var isWatched: Boolean = false,
    override val file: FileSource
) : FluxArtwork {

    @Transient
    @Ignore
    val releaseDate: Date? = releaseDateString.parseTMDBDate()

    constructor(
        showId: Int,
        tmdbEpisode: TMDBEpisode,
        file: FileSource
    ) : this (
        id = tmdbEpisode.id,
        showId = showId,
        title = tmdbEpisode.title,
        number = tmdbEpisode.number,
        season = tmdbEpisode.season,
        imagePath = tmdbEpisode.imagePath,
        releaseDateString = tmdbEpisode.releaseDateString,
        crew = tmdbEpisode.crew,
        description = tmdbEpisode.description,
        duration = tmdbEpisode.duration,
        voteAverage = tmdbEpisode.voteAverage,
        voteCount = tmdbEpisode.voteCount,
        isWatched = false,
        file = file
    )
}
