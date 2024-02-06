package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.FluxArtwork
import com.kaem.flux.model.flux.FluxEpisode
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseArtworkDataSource @Inject constructor(
    private val databaseManager: DatabaseManager
) : ArtworkDataSource {

    override suspend fun getArtworks(
        files: List<UserFile>,
        artworkIds: List<Int>
    ): Pair<List<FluxArtwork>, List<FluxEpisode>> {

        val artworks = arrayListOf<FluxArtwork>()
        val episodes = arrayListOf<FluxEpisode>()

        coroutineScope {

            launch {

                val dbArtworks = withContext(Dispatchers.Default) { databaseManager.getAllArtworks() }
                artworks.addAll(dbArtworks)

            }

            launch {

                val dbEpisodes = withContext(Dispatchers.Default) { databaseManager.getAllEpisodes() }
                episodes.addAll(dbEpisodes)

            }

        }

        val (filteredArtworks, filteredEpisodes) = cleanDatabase(
            files = files,
            artworks = artworks,
            episodes = episodes
        )

        return Pair(filteredArtworks, filteredEpisodes)

    }

    private suspend fun cleanDatabase(
        files: List<UserFile>,
        artworks: List<FluxArtwork>,
        episodes: List<FluxEpisode>
    ) : Pair<List<FluxArtwork>, List<FluxEpisode>> {

        val filePaths = files.map { it.path }
        val episodesToRemove = arrayListOf<FluxEpisode>()
        val moviesToRemove = arrayListOf<FluxMovie>()
        val showsToRemove = arrayListOf<FluxShow>()

        episodes.forEach {

            if (!filePaths.contains(it.file.path))
                episodesToRemove.add(it)
        }

        artworks.filterIsInstance<FluxMovie>().forEach {

            if (!filePaths.contains(it.file.path))
                moviesToRemove.add(it)

        }

        artworks.filterIsInstance<FluxShow>().forEach { show ->

            if (episodes.none { it.showId == show.id })
                showsToRemove.add(show)

        }

        withContext(Dispatchers.Default) {

            launch { databaseManager.deleteMovies(moviesToRemove.map { it.id }) }

            launch { databaseManager.deleteShows(showsToRemove.map { it.id }) }

            launch { databaseManager.deleteEpisodes(episodesToRemove.map { it.id }) }

        }



        return Pair(
            artworks.filterNot { showsToRemove.contains(it) || moviesToRemove.contains(it) },
            episodes.filterNot { episodesToRemove.contains(it) },
        )

    }

}