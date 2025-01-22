package com.kaem.flux.data.source.artwork

import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.UserFolder
import com.kaem.flux.model.artwork.ArtworkOverview
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.tmdb.TMDBMediaType
import com.kaem.flux.utils.Analytics
import com.kaem.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtworkDataSourceTMDBImpl @Inject constructor(
    private val tmdbService: TMDBService,
    private val firebaseAnalytics: FirebaseAnalytics
) : ArtworkDataSource {

    //region Companion object

    companion object {
        const val TAG = "ArtworkDataSourceTMDB"
    }

    //endregion

    //region Public methods

    override suspend fun getArtworks(
        files: List<UserFile>,
        sync: Boolean
    ): ArtworkDataSource.Library {

        var movies: Map<ArtworkOverview, Movie> = mapOf()
        var shows: Map<ArtworkOverview, List<Episode>> = mapOf()

        withContext(Dispatchers.Default) {

            val folders = files.groupInFolders()

            coroutineScope {

                launch {
                    val moviesFolders = folders.filter { it.type == ContentType.MOVIE }
                    movies = getMovies(moviesFolders)
                }

                launch {
                    val showsFolders = folders.filter { it.type == ContentType.SHOW }
                    shows = getShows(showsFolders)
                }

            }

        }

        return ArtworkDataSource.Library(
            overviews = (movies.keys + shows.keys).toList(),
            movies = movies.values.toList(),
            episodes = shows.values.flatten()
        ).also {
            Log.d(TAG, "[getArtworks] Found ${it.overviews.size} overviews, ${it.movies.size} movies, ${it.episodes.size} episodes")
        }
    }

    //endregion

    //region Private methods

    private suspend fun getMovies(folders: List<UserFolder>) : Map<ArtworkOverview, Movie> {

        val movies = coroutineScope {

            folders.map { folder ->

                async {

                    try {

                        val file = folder.files.first()

                        val tmdbOverviews = tmdbService.getMovie(
                            title = folder.title,
                            year = file.nameProperties.year
                        )

                        val tmdbOverview = tmdbOverviews.results.maxBy { it.popularity }.also {
                            it.type = TMDBMediaType.MOVIE
                        }

                        val tmdbMovie = tmdbService.getMovieDetails(id = tmdbOverview.id)
                        val overview = ArtworkOverview(tmdbMovie = tmdbMovie)
                        val movie = Movie(tmdbMovie = tmdbMovie, file = file)

                        overview to movie

                    } catch (e: Exception) {
                        Log.i(TAG, "[getMovies] Fail to get movie : ${folder.title}", e)
                        firebaseAnalytics.logEvent(Analytics.Event.TMDB_ERROR) {
                            param(Analytics.Param.TITLE, folder.title)
                            param(Analytics.Param.TYPE, "movie")
                            param(Analytics.Param.MESSAGE, e.message ?: "Unknown")
                        }
                        null
                    }

                }

            }.awaitAll().filterNotNull().toMap()

        }

        Log.i(TAG, "[getShows] Found ${movies.size}/${folders.size} movies")

        return movies

    }

    private suspend fun getShows(folders: List<UserFolder>) : Map<ArtworkOverview, List<Episode>> {

        val shows = mutableMapOf<ArtworkOverview, List<Episode>>()

        coroutineScope {

            folders.map { folder ->

                async {

                    getShowOverviewAndEpisodes(folder)?.let { (overview, episodes) ->
                        shows[overview] = episodes
                    }

                }

            }.awaitAll()

        }

        Log.i(TAG, "[getShows] Found ${shows.size}/${folders.size} shows and ${shows.values.flatten().size}/${folders.map { it.files }.flatten().size} episodes")

        return shows

    }

    private suspend fun getShowOverviewAndEpisodes(folder: UserFolder) : Pair<ArtworkOverview, List<Episode>>? {

        val overview: ArtworkOverview

        try {

            val tmdbOverviews = tmdbService.getShow(
                title = folder.title,
                year = folder.files.firstOrNull { it.nameProperties.year != null }?.nameProperties?.year
            )

            val tmdbOverview = tmdbOverviews.results.maxBy { it.popularity }.also {
                it.type = TMDBMediaType.SHOW
            }

            overview = ArtworkOverview(tmdbOverview)

        } catch (e: Exception) {
            Log.e(TAG, "[getShowAndEpisodes] Fail to get show overview : ${folder.title}", e)
            firebaseAnalytics.logEvent(Analytics.Event.TMDB_ERROR) {
                param(Analytics.Param.TITLE, folder.title)
                param(Analytics.Param.TYPE, "show overview")
                param(Analytics.Param.MESSAGE, e.message ?: "Unknown")
            }
            return null
        }

        val episodes: List<Episode> = coroutineScope {

            folder.files.map { file ->

                async {

                    try {

                        val tmdbEpisode = tmdbService.getEpisode(
                            id = overview.id,
                            season = file.nameProperties.season!!,
                            episode = file.nameProperties.episode!!
                        )

                        Episode(
                            tmdbEpisode = tmdbEpisode,
                            artworkId = overview.id,
                            file = file
                        )

                    } catch (e: Exception) {
                        Log.e(TAG, "[getShowAndEpisodes] Fail to get episode : ${folder.title} (season ${file.nameProperties.season}, episode ${file.nameProperties.episode})", e)
                        firebaseAnalytics.logEvent(Analytics.Event.TMDB_ERROR) {
                            param(Analytics.Param.TITLE, folder.title)
                            param(Analytics.Param.SEASON, file.nameProperties.season.toString())
                            param(Analytics.Param.EPISODE, file.nameProperties.episode.toString())
                            param(Analytics.Param.TYPE, "show episode")
                            param(Analytics.Param.MESSAGE, e.message ?: "Unknown")
                        }
                        null
                    }

                }

            }.awaitAll().filterNotNull()

        }

        return overview to episodes

    }

}