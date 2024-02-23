package com.kaem.flux.model.flux

import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBCrew
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

data class Artwork(
    val id: Int,
    val title: String,
    val imagePath: String,
    val bannerPath: String,
    val content: ArtworkContent
) {

    val description: String? = when (content) {
        is ArtworkContent.Movie -> content.movie.description
        is ArtworkContent.Show -> content.currentEpisode?.description
    }

}


sealed class ArtworkContent {

    data class Movie(val movie: ArtworkInfo.Movie) : ArtworkContent()

    data class Show(val episodes: List<ArtworkInfo.Episode>) : ArtworkContent() {

        val currentEpisode get() = episodes.lastOrNull { it.status == FluxStatus.IS_WATCHING }
            ?: episodes.firstOrNull { it.status == FluxStatus.TO_WATCH }
            ?: episodes.firstOrNull()

    }

}

sealed class ArtworkInfo(
    val releaseDateString: String,
    val description: String,
    val voteAverage: Float,
    val voteCount: Int,
    val duration: Int,
    val file: UserFile,
    var status: FluxStatus = FluxStatus.TO_WATCH,
) {

    val releaseDate: Date? get() = releaseDateString.parseTMDBDate()

    class Movie(
        releaseDateString: String,
        description: String,
        voteAverage: Float,
        voteCount: Int,
        duration: Int,
        file: UserFile,
        status: FluxStatus,
        val genres: List<String> = listOf()
    ) : ArtworkInfo(
        releaseDateString = releaseDateString,
        description = description,
        voteAverage = voteAverage,
        voteCount = voteCount,
        duration = duration,
        file = file,
        status = status
    )

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
    )

}
