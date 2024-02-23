package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBCrew
import com.kaem.flux.model.tmdb.TMDBEpisode

class Episode(
    val id: Int,
    val title: String,
    val showId: Int,
    val number: Int,
    val season: Int,
    val imagePath: String,
    val crew: List<TMDBCrew>,
    releaseDateString: String,
    description: String,
    duration: Int,
    voteAverage: Float,
    voteCount: Int,
    status: FluxStatus = FluxStatus.TO_WATCH,
    file: UserFile
) : ArtworkInfo(
    releaseDateString = releaseDateString,
    description = description,
    voteAverage = voteAverage,
    voteCount = voteCount,
    duration = duration,
    file = file,
    status = status
) {

    constructor(
        showId: Int,
        tmdbEpisode: TMDBEpisode,
        file: UserFile
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
        status = FluxStatus.TO_WATCH,
        file = file
    )

}
