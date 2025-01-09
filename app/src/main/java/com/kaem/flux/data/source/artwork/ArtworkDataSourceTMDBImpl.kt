package com.kaem.flux.data.source.artwork

import android.util.Log
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.UserFolder
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.tmdb.TMDBOverview
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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
                            tmdbOverview = tmdbArtwork,
                            file = file
                        )

                        if (tmdbArtwork?.type == TMDBMediaType.SHOW) {
                            tmdbToFluxEpisode(
                                tmdbOverview = tmdbArtwork,
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

    fun createFolders(userFiles: List<UserFile>) : List<UserFolder> {

        val folders = userFiles.groupBy { it.nameProperties.title }.map { (title, files) ->

            val seasonsAndEpisodes = files.mapNotNull {

                val season = it.nameProperties.season
                val episode = it.nameProperties.episode

                if (season != null && episode != null)
                    season to episode
                else
                    null

            }

            UserFolder(
                title = title,
                year = files.first().nameProperties.year,
                seasonsAndEpisodes = seasonsAndEpisodes,
                files = userFiles
            )

        }

        return folders

    }

    suspend fun getTMDBOverviews(folders: List<UserFolder>) : List<TMDBOverview> {

        var tmdbOverviews = emptyList<TMDBOverview>()

        CoroutineScope(Dispatchers.Default).launch {

            tmdbOverviews = folders.map { folder ->

                async {

                    val type = if (folder.seasonsAndEpisodes.isEmpty()) TMDBMediaType.MOVIE else TMDBMediaType.SHOW

                    try {

                        val artworks = if (type == TMDBMediaType.SHOW) {
                            tmdbService.getShow(
                                title = folder.title,
                                year = folder.year
                            )
                        } else {
                            tmdbService.getMovie(
                                title = folder.title,
                                year = folder.year
                            )
                        }

                        artworks.results.maxByOrNull { it.popularity }?.also {
                            it.type = type
                        }

                    } catch (e: Exception) {
                        Log.i(TAG, "[getTMDBOverviews] Fail to get TMDB Overviews : ${folder.title}")
                        null
                    }

                }

            }.awaitAll().filterNotNull()

        }

        return tmdbOverviews

    }

    private suspend fun tmdbToFluxOverviews(tmdbOverviews: List<TMDBOverview>) : List<ArtworkOverview> {

        var overviews = emptyList<ArtworkOverview>()

        CoroutineScope(Dispatchers.Default).launch {

            overviews = tmdbOverviews.map { tmdbOverview ->

                async {

                    try {

                        when (tmdbOverview.type) {

                            TMDBMediaType.MOVIE -> {

                                val tmdbMovie = tmdbService.getMovieDetails(id = tmdbOverview.id)
                                ArtworkOverview(tmdbMovie = tmdbMovie)

                            }

                            TMDBMediaType.SHOW -> {

                                ArtworkOverview(tmdbOverview = tmdbOverview)

                            }

                            else -> null

                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "[tmdbToFluxOverviews] Fail to get details for ${tmdbOverview.title} (${tmdbOverview.id})", e)
                        null
                    }

                }

            }.awaitAll().filterNotNull()

        }

        return overviews

    }

    private suspend fun getTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBOverview? {

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
        tmdbOverview: TMDBOverview?,
        file: UserFile
    ) {

        tmdbOverview ?: return

        when (tmdbOverview.type){

            TMDBMediaType.MOVIE -> {

                try {

                    val tmdbMovie = tmdbService.getMovieDetails(id = tmdbOverview.id)

                    val artworkOverview = ArtworkOverview(tmdbMovie = tmdbMovie)
                    addArtwork(artworkOverview)

                    val movie = Movie(tmdbMovie = tmdbMovie, file = file)

                    addMovie(movie)

                    Log.i(TAG, "[tmdbToFluxArtwork] Movie : ${movie.title} (id ${movie.artworkId})")

                } catch (e: Exception) {
                    Log.e(TAG, "Fail to get movie details for ID ${tmdbOverview.id}", e)
                }

            }

            TMDBMediaType.SHOW -> {

                val show = ArtworkOverview(tmdbOverview = tmdbOverview)
                addArtwork(show)

                Log.i(TAG, "[tmdbToFluxArtwork] Show : ${show.title} (id ${show.id})")

            }

            else -> {}

        }

    }

    private suspend fun tmdbToFluxEpisode(
        tmdbOverview: TMDBOverview?,
        file: UserFile
    ) {

        tmdbOverview ?: return

        try {

            val tmdbEpisode = tmdbService.getEpisode(
                id = tmdbOverview.id,
                season = file.nameProperties.season!!,
                episode = file.nameProperties.episode!!
            )

            val episode = Episode(
                tmdbEpisode = tmdbEpisode,
                artworkId = tmdbOverview.id,
                file = file
            )

            addEpisode(episode)

            Log.i(TAG, "[tmdbToFluxEpisode] Episode : ${episode.title} season ${episode.season} n°${episode.number} (id ${episode.id})")

        } catch (e: Exception) {
            Log.e(TAG, "Fail to get episode details for ID ${tmdbOverview.id}, season ${file.nameProperties.season}, episode ${file.nameProperties.episode}", e)
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