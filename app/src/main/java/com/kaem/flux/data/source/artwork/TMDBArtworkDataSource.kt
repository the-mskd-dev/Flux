package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.data.tmdb.TMDBService
import com.kaem.flux.model.FileNameProperties
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.tmdb.TMDBArtwork
import com.kaem.flux.model.tmdb.TMDBMediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TMDBArtworkDataSource @Inject constructor(
    private val databaseManager: DatabaseManager,
    private val tmdbService: TMDBService
) : ArtworkDataSource {

    private val mutexArtworks = Mutex()
    private val artworks = arrayListOf<Artwork>()

    private val mutexEpisodes = Mutex()
    private val episodes = arrayListOf<Episode>()

    override suspend fun getArtworks(
        files: List<UserFile>,
        artworkIds: List<Int>
    ): Pair<List<Artwork>, List<Episode>> {

        getArtworksFromFiles(files = files)

        artworks.removeAll { artworkIds.contains(it.id) }
        episodes.removeAll { artworkIds.contains(it.id) }

        withContext(Dispatchers.Default) {

            launch {
                saveInDatabase()
            }

        }

        return Pair(artworks, episodes)
    }

    private suspend fun saveInDatabase() {

        withContext(Dispatchers.Default) {

            launch {

                databaseManager.saveArtworks(artworks.toList())

            }

            launch {

                databaseManager.saveEpisodes(episodes.toList())

            }

        }

    }

    private suspend fun getArtworksFromFiles(files: List<UserFile>) {

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

    private suspend fun getTmdbArtwork(fileNameProperties: FileNameProperties) : TMDBArtwork? {

        return if (fileNameProperties.episode != null && fileNameProperties.season != null) {

            val artworks = tmdbService.getShow(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            val artwork = artworks.results.maxBy { it.popularity }
            artwork.type = TMDBMediaType.SHOW

            artwork

        } else {

            val artworks = tmdbService.getMovie(
                title = fileNameProperties.title,
                year = fileNameProperties.year
            )

            val artwork = artworks.results.firstOrNull()
            artwork?.type = TMDBMediaType.MOVIE

            artwork

        }

    }

    private suspend fun tmdbToFluxArtwork(
        tmdbArtwork: TMDBArtwork?,
        file: UserFile
    ) {

        tmdbArtwork ?: return

        when (tmdbArtwork.type){

            TMDBMediaType.MOVIE -> {


                val tmdbMovie = tmdbService.getMovieDetails(
                    id = tmdbArtwork.id
                )

                val movie = Artwork(
                    tmdbMovie = tmdbMovie,
                    file = file,
                )

                addArtworkSummary(movie)

            }

            TMDBMediaType.SHOW -> {

                val show = Artwork(tmdbArtwork = tmdbArtwork)
                addArtworkSummary(show)

            }

            else -> {}

        }

    }

    private suspend fun tmdbToFluxEpisode(
        tmdbArtwork: TMDBArtwork?,
        file: UserFile
    ) {

        tmdbArtwork ?: return

        val tmdbEpisode = tmdbService.getEpisode(
            id = tmdbArtwork.id,
            season = file.nameProperties.season!!,
            episode = file.nameProperties.episode!!
        )

        val episode = Episode(
            tmdbEpisode = tmdbEpisode,
            showId = tmdbArtwork.id,
            file = file
        )

        addEpisode(episode)

    }

    //endregion

    //region Lists

    private suspend fun addArtworkSummary(artworkSummary: Artwork) {
        mutexArtworks.withLock {
            if (artworks.none { it.id == artworkSummary.id })
                artworks.add(artworkSummary)
        }
    }

    private suspend fun addEpisode(episode: Episode) {
        mutexEpisodes.withLock {
            if (episodes.none { it.id == episode.id })
                episodes.add(episode)
        }
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        // Nothing to do here
    }

    //endregion

}