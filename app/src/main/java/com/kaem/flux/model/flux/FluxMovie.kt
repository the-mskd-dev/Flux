package com.kaem.flux.model.flux

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.tmdb.TMDBMovie
import com.kaem.flux.utils.parseTMDBDate
import java.util.Date

@Entity
data class FluxMovie(
    @PrimaryKey override val id: Int,
    @ColumnInfo override val title: String,
    @ColumnInfo override val imagePath: String,
    @ColumnInfo override val bannerPath: String,
    @ColumnInfo override val releaseDateString: String,
    @ColumnInfo override val description: String,
    @ColumnInfo override val voteAverage: Float,
    @ColumnInfo override val voteCount: Int,
    @ColumnInfo override val duration: Int,
    @ColumnInfo override var isWatched: Boolean = false,
    @ColumnInfo override val fileName: String,
    @ColumnInfo override val filePath: String,
    //@ColumnInfo val genres: List<String> = listOf(),
) : FluxArtworkSummary, FluxArtwork {

    @Transient
    @Ignore
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
        //genres = emptyList(),
        fileName = file.name,
        filePath = when (file) {
            is FileSource.Local -> file.uri.toString()
            is FileSource.GDrive -> file.path
        }
    )
}
