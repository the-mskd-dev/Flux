package com.kaem.flux.model.flux

import com.kaem.flux.model.FileSource
import com.kaem.flux.model.tmdb.TMDBCrew
import com.kaem.flux.model.tmdb.TMDBEpisode

data class FluxEpisode(
    val id: Int,
    val name: String,
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

    constructor(
        tmdbEpisode: TMDBEpisode,
        file: FileSource
    ) : this (
        id = tmdbEpisode.id,
        name = tmdbEpisode.name,
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
