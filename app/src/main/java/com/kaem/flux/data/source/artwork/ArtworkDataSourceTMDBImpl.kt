package com.kaem.flux.data.source.artwork

import android.util.Log
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class ArtworkDataSourceTMDBImpl @Inject constructor(private val tmdbService: TMDBService) : ArtworkDataSource {

    private val mutexArtworks = Mutex()
    private val artworkOverviews = arrayListOf<ArtworkOverview>()

    private val mutexMovies = Mutex()
    private val movies = arrayListOf<Movie>()

    private val mutexEpisodes = Mutex()
    private val episodes = arrayListOf<Episode>()

    override suspend fun getArtworks(
        files: List<UserFile>,
        artworkIds: List<Long>,
        sync: Boolean
    ): ArtworkDataSource.Library {

        coroutineScope {

            files.forEach { file ->

                launch {

                    val tmdbArtwork = getTmdbArtwork(file.nameProperties)
                    tmdbToFluxArtwork(
                        tmdbArtwork = tmdbArtwork,
                        file = file
                    )

                    if (tmdbArtwork?.type == TMDBMediaType.SHOW) {
                        tmdbToFluxEpisode(
                            tmdbArtwork = tmdbArtwork,
                            file = file
                        )
                    }

                }

            }

        }

        return ArtworkDataSource.Library(
            artworkOverviews = artworkOverviews,
            movies = movies,
            episodes = episodes
        )

    }

    private suspend fun getTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

        return if (fileNameProperties.episode != null && fileNameProperties.season != null) {

            try {

                val artworks = tmdbService.getShow(
                    title = fileNameProperties.title,
                    year = fileNameProperties.year
                )

                val artwork = artworks.results.maxBy { it.popularity }
                artwork.type = TMDBMediaType.SHOW

                artwork

            } catch (e: Exception) {
                Log.e("ArtworkDataSourceTMDBImpl", "Fail to get show", e)
                null
            }

        } else {

            try {

                val artworks = tmdbService.getMovie(
                    title = fileNameProperties.title,
                    year = fileNameProperties.year
                )

                val artwork = artworks.results.firstOrNull()
                artwork?.type = TMDBMediaType.MOVIE

                artwork

            } catch (e: Exception) {
                Log.e("ArtworkDataSourceTMDBImpl", "Fail to get movie", e)
                null
            }

        }

    }

    private suspend fun tmdbToFluxArtwork(
        tmdbArtwork: TMDBArtwork?,
        file: UserFile
    ) {

        tmdbArtwork ?: return

        when (tmdbArtwork.type){

            TMDBMediaType.MOVIE -> {

                try {

                    val tmdbMovie = tmdbService.getMovieDetails(id = tmdbArtwork.id)

                    val artworkOverview = ArtworkOverview(tmdbMovie = tmdbMovie)
                    addArtwork(artworkOverview)

                    val movie = Movie(tmdbMovie = tmdbMovie, file = file,)
                    addMovie(movie)

                } catch (e: Exception) {
                    Log.e("ArtworkDataSourceTMDBImpl", "Fail to get movie details", e)
                }


            }

            TMDBMediaType.SHOW -> {

                val show = ArtworkOverview(tmdbArtwork = tmdbArtwork)
                addArtwork(show)

            }

            else -> {}

        }

    }

    private suspend fun tmdbToFluxEpisode(
        tmdbArtwork: TMDBArtwork?,
        file: UserFile
    ) {

        tmdbArtwork ?: return

        try {

            val tmdbEpisode = tmdbService.getEpisode(
                id = tmdbArtwork.id,
                season = file.nameProperties.season!!,
                episode = file.nameProperties.episode!!
            )

            val episode = Episode(
                tmdbEpisode = tmdbEpisode,
                artworkId = tmdbArtwork.id,
                file = file
            )

            addEpisode(episode)

        } catch (e: Exception) {
            Log.e("ArtworkDataSourceTMDBImpl", "Fail to get episode details", e)
        }

    }

    //endregion

    //region Lists

    private suspend fun addArtwork(artworkOverview: ArtworkOverview) {
        mutexArtworks.withLock {
            if (artworkOverviews.none { it.id == artworkOverview.id })
                artworkOverviews.add(artworkOverview)
        }
    }

    private suspend fun addMovie(movie: Movie) {
        mutexMovies.withLock {
            if (movies.none { it.artworkId == movie.artworkId })
                movies.add(movie)
        }
    }

    private suspend fun addEpisode(episode: Episode) {
        mutexEpisodes.withLock {
            if (episodes.none { it.id == episode.id })
                episodes.add(episode)
        }
    }

    //endregion

}