package com.kaem.flux.data.source.media

import android.util.Log
import com.google.gson.JsonSyntaxException
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.UserFolder
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import com.kaem.flux.model.tmdb.TMDBMediaType
import com.kaem.flux.utils.extensions.groupInFolders
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaSourceTMDBImpl @Inject constructor(private val tmdbService: TMDBService) : MediaSource {

    //region Companion object

    companion object {
        const val TAG = "MediaDataSourceTMDB"
    }

    //endregion

    //region Public methods

    override suspend fun getMedias(files: List<UserFile>): MediaSource.Library {

        var movies: Map<Artwork, Movie> = mapOf()
        var shows: Map<Artwork, List<Episode>> = mapOf()

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

        return MediaSource.Library(
            artworks = (movies.keys + shows.keys).toList(),
            movies = movies.values.toList(),
            episodes = shows.values.flatten()
        ).also {
            Log.d(TAG, "[getMedias] Found ${it.artworks.size} artworks, ${it.movies.size} movies, ${it.episodes.size} episodes")
        }
    }

    //endregion

    //region Private methods

    private suspend fun getMovies(folders: List<UserFolder>) : Map<Artwork, Movie> {

        val movies = coroutineScope {

            folders.map { folder ->

                async {

                    try {

                        val file = folder.files.first()

                        val tmdbArtworks = tmdbService.getMovie(
                            title = folder.title,
                            year = file.nameProperties.year
                        )

                        val tmdbArtwork = tmdbArtworks.results.maxBy { it.popularity }.also {
                            it.type = TMDBMediaType.MOVIE
                        }

                        val tmdbMovie = tmdbService.getMovieDetails(id = tmdbArtwork.id)
                        val artwork = Artwork(tmdbMovie = tmdbMovie)
                        val movie = Movie(tmdbMovie = tmdbMovie, file = file)

                        artwork to movie

                    } catch (e: Exception) {
                        Log.e(TAG, "[getMovies] Fail to get movie : ${folder.title}", e)
                        null
                    }

                }

            }.awaitAll().filterNotNull().toMap()

        }

        Log.i(TAG, "[getShows] Found ${movies.size}/${folders.size} movies")

        return movies

    }

    private suspend fun getShows(folders: List<UserFolder>) : Map<Artwork, List<Episode>> {

        val shows = mutableMapOf<Artwork, List<Episode>>()

        coroutineScope {

            folders.map { folder ->

                async {

                    getShowArtwork(folder = folder)?.let { artwork ->
                        val episodes = getEpisodes(folder = folder, artwork = artwork)
                        shows[artwork] = episodes
                    }

                }

            }.awaitAll()

        }

        Log.i(TAG, "[getShows] Found ${shows.size}/${folders.size} shows and ${shows.values.flatten().size}/${folders.flatMap { it.files }.size} episodes")

        return shows

    }

    private suspend fun getShowArtwork(folder: UserFolder) : Artwork? {

        return try {

            val tmdbArtworks = tmdbService.getShow(
                title = folder.title,
                year = folder.files.firstOrNull { it.nameProperties.year != null }?.nameProperties?.year
            )

            val tmdbArtwork = tmdbArtworks.results.maxBy { it.popularity }.also {
                it.type = TMDBMediaType.SHOW
            }

            Artwork(tmdbArtwork)

        } catch (e: Exception) {
            Log.e(TAG, "[getShowAndEpisodes] Fail to get show artwork : ${folder.title}", e)
            null
        }

    }

    private suspend fun getEpisodes(folder: UserFolder, artwork: Artwork) : List<Episode> {

        return coroutineScope {

            folder.files.map { file ->

                async {

                    try {

                        val tmdbEpisode = tmdbService.getEpisode(
                            id = artwork.id,
                            season = file.nameProperties.season!!,
                            episode = file.nameProperties.episode!!
                        )

                        Episode(
                            tmdbEpisode = tmdbEpisode,
                            mediaId = artwork.id,
                            file = file
                        )

                    } catch (e: Exception) {
                        Log.e(TAG, "[getShowAndEpisodes] Fail to get episode : ${folder.title} (season ${file.nameProperties.season}, episode ${file.nameProperties.episode})", e)
                        null
                    }

                }

            }.awaitAll().filterNotNull()

        }

    }

}