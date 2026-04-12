package com.mskd.flux.data.source.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.util.Log
import androidx.core.net.toUri
import com.mskd.flux.data.tmdb.TMDBService
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.UserFolder
import com.mskd.flux.model.artwork.Artwork
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.model.artwork.Episode
import com.mskd.flux.model.artwork.Media
import com.mskd.flux.model.artwork.Movie
import com.mskd.flux.model.tmdb.TMDBMediaType
import com.mskd.flux.utils.extensions.groupInFolders
import com.mskd.flux.utils.extensions.msToMin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaSourceTMDBImpl @Inject constructor(
    private val tmdbService: TMDBService,
    private val context: Context
) : MediaSource {

    //region Variables

    private val limitedDispatcher = Dispatchers.IO.limitedParallelism(10)

    //endregion

    //region Companion object

    companion object {
        const val TAG = "MediaDataSourceTMDB"
    }

    //endregion

    //region Public methods

    override suspend fun getMedias(files: List<UserFile>): MediaSource.Library {

        var movies: Map<Artwork, List<Media>> = mapOf()
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

        val moviesFiltered = movies.values.flatten().filterIsInstance<Movie>()
        val unknownMedias = movies.values.flatten().filterIsInstance<Episode>().filter { it.isUnknown }

        return MediaSource.Library(
            artworks = (movies.keys + shows.keys).distinctBy { it.id }.toList(),
            movies = moviesFiltered,
            episodes = shows.values.flatten() + unknownMedias
        ).also {
            Log.d(TAG, "[getMedias] Found ${it.artworks.size} artworks, ${it.movies.size} movies, ${it.episodes.size} episodes")
        }
    }

    //endregion

    //region Private methods

    private suspend fun getMovies(folders: List<UserFolder>) : Map<Artwork, List<Media>> = withContext(limitedDispatcher) {

        val movies = coroutineScope {

            folders.map { folder ->

                async {

                    val file = folder.files.first()

                    try {

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
                        val unknownMedia = getUnknownMediaFrom(file = file)
                        Artwork.UNKNOWN to unknownMedia

                    }

                }

            }.awaitAll()

        }

        val groupedMovies = movies.groupBy(
            keySelector = { it.first },
            valueTransform = { it.second }
        )

        Log.i(TAG, "[getMovies] Found ${groupedMovies.values.flatten().size}/${folders.size} movies")

        groupedMovies

    }

    private suspend fun getShows(folders: List<UserFolder>) : Map<Artwork, List<Episode>> = withContext(limitedDispatcher) {

        val shows = coroutineScope {

            folders.map { folder ->

                async {

                    val artwork = getShowArtwork(folder = folder)
                    val episodes = getEpisodes(folder = folder, artwork = artwork)
                    artwork to episodes

                }

            }.awaitAll()

        }

        val groupedShows = shows
            .flatMap { (artwork, episodes) -> episodes.map { artwork to it } }
            .groupBy(
                keySelector = { it.first },
                valueTransform = { it.second }
            )

        Log.i(TAG, "[getShows] Found ${groupedShows.size}/${folders.size} shows and ${groupedShows.values.flatten().size}/${folders.flatMap { it.files }.size} episodes")

        groupedShows

    }

    private suspend fun getShowArtwork(folder: UserFolder) : Artwork {

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
            Artwork.UNKNOWN
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
                        getUnknownMediaFrom(file = file)
                    }

                }

            }.awaitAll()

        }

    }

    private suspend fun getUnknownMediaFrom(file: UserFile) : Episode = withContext(limitedDispatcher) {

        val retriever = MediaMetadataRetriever()

        try {

            val duration = context.contentResolver.openAssetFileDescriptor(file.path.toUri(), "r")?.use { afd ->
                retriever.setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                val durationInMs = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLong() ?: 0L
                durationInMs.msToMin.toInt()
            } ?: 0

            Episode(file = file, duration = duration)

        } catch (e: Exception) {

            Log.e(TAG, "[getUnknownMediaFrom] Fail to get duration for ${file.path}", e)
            Episode(file = file)

        } finally {

            retriever.release()

        }

    }

}