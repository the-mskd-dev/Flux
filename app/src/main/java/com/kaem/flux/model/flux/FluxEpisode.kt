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

@Entity
data class FluxEpisode(
    @PrimaryKey val id: Int,
    @ColumnInfo val showId: Int,
    @ColumnInfo val title: String,
    @ColumnInfo val number: Int,
    @ColumnInfo val season: Int,
    @ColumnInfo val imagePath: String,
    @ColumnInfo val releaseDateString: String,
    //@ColumnInfo val crew: List<TMDBCrew>,
    @ColumnInfo override val description: String,
    @ColumnInfo override val duration: Int,
    @ColumnInfo override val voteAverage: Float,
    @ColumnInfo override val voteCount: Int,
    @ColumnInfo override var isWatched: Boolean = false,
    @ColumnInfo override val fileName: String,
    @ColumnInfo override val filePath: String
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
        //crew = tmdbEpisode.crew,
        description = tmdbEpisode.description,
        duration = tmdbEpisode.duration,
        voteAverage = tmdbEpisode.voteAverage,
        voteCount = tmdbEpisode.voteCount,
        isWatched = false,
        fileName = file.name,
        filePath = when (file) {
            is FileSource.Local -> file.uri.toString()
            is FileSource.GDrive -> file.path
        }
    )
}
