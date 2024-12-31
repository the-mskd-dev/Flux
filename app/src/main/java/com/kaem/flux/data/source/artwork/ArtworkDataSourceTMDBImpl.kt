package com.kaem.flux.data.source.artwork

import android.util.Log
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtworkDataSourceTMDBImpl @Inject constructor(private val tmdbService: TMDBService) : ArtworkDataSource {

    //region Variables

    private val mutexOverviews = Mutex()
    private val overviews = arrayListOf<ArtworkOverview>()

    private val mutexMovies = Mutex()
    private val movies = arrayListOf<Movie>()

    private val mutexEpisodes = Mutex()
    private val episodes = arrayListOf<Episode>()

    private var savedOverviewIds: List<Long> = emptyList()

    //endregion

    //region Companion object

    companion object {
        const val TAG = "ArtworkDataSourceTMDB"
    }

    //endregion

    //region Public methods

    override suspend fun getArtworks(
        files: List<UserFile>,
        overviewIds: List<Long>,
        sync: Boolean
    ): ArtworkDataSource.Library {

        savedOverviewIds = overviewIds
        overviews.clear()
        movies.clear()
        episodes.clear()

        withContext(Dispatchers.Default) {
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
        }

        return ArtworkDataSource.Library(
            overviews = overviews.filter { a -> if (a.type == ContentType.SHOW) episodes.any { it.artworkId == a.id } else true },
            movies = movies,
            episodes = episodes
        )

    }

    //endregion

    //region Private methods

    private suspend fun getTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

        return if (fileNameProperties.episode != null && fileNameProperties.season != null) {

            try {

                val artworks = tmdbService.getShow(
                    title = fileNameProperties.title,
                    year = fileNameProperties.year
                )

                val show = artworks.results.maxByOrNull { it.popularity }?.also {
                    it.type = TMDBMediaType.SHOW
                }

                Log.i(TAG, "[getTmdbArtwork] Show : ${show?.title} (id ${show?.id})")

                show

            } catch (e: Exception) {
                Log.e(TAG, "[getTmdbArtwork] Fail to get show for name ${fileNameProperties.title}", e)
                null
            }

        } else {

            try {

                val artworks = tmdbService.getMovie(
                    title = fileNameProperties.title,
                    year = fileNameProperties.year
                )

                val movie = artworks.results.firstOrNull()?.also {
                    it.type = TMDBMediaType.MOVIE
                }

                Log.i(TAG, "[getTmdbArtwork] Movie : ${movie?.title} (id ${movie?.id})")

                movie

            } catch (e: Exception) {
                Log.e(TAG, "[getTmdbArtwork] Fail to get movie for name ${fileNameProperties.title}", e)
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

                    val movie = Movie(tmdbMovie = tmdbMovie, file = file)

                    addMovie(movie)

                    Log.i(TAG, "[tmdbToFluxArtwork] Movie : ${movie.title} (id ${movie.artworkId})")

                } catch (e: Exception) {
                    Log.e(TAG, "Fail to get movie details for ID ${tmdbArtwork.id}", e)
                }

            }

            TMDBMediaType.SHOW -> {

                val show = ArtworkOverview(tmdbArtwork = tmdbArtwork)
                addArtwork(show)

                Log.i(TAG, "[tmdbToFluxArtwork] Show : ${show.title} (id ${show.id})")

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

            Log.i(TAG, "[tmdbToFluxEpisode] Episode : ${episode.title} season ${episode.season} n°${episode.number} (id ${episode.id})")

        } catch (e: Exception) {
            Log.e(TAG, "Fail to get episode details for ID ${tmdbArtwork.id}, season ${file.nameProperties.season}, episode ${file.nameProperties.episode}", e)
        }

    }

    //endregion

    //region Lists methods

    private suspend fun addArtwork(artworkOverview: ArtworkOverview) {
        mutexOverviews.withLock {
            if (overviews.none { it.id == artworkOverview.id } && !savedOverviewIds.contains(artworkOverview.id))
                overviews.add(artworkOverview)
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