package com.kaem.flux.model.flux

import android.os.Parcelable
import androidx.room.Ignore
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.tmdb.TMDBCrew
import com.kaem.flux.model.tmdb.TMDBEpisode
import com.kaem.flux.utils.parseTMDBDate
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Date

@Parcelize
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
    override var status: FluxStatus = FluxStatus.TO_WATCH,
    override val file: UserFile
) : FluxArtworkDetails, Parcelable {

    @IgnoredOnParcel
    @Transient
    @Ignore
    val releaseDate: Date? = releaseDateString.parseTMDBDate()

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
