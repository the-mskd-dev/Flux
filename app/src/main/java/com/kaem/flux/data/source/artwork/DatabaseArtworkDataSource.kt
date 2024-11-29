package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork
import com.kaem.flux.model.flux.Content
import com.kaem.flux.model.flux.Episode
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
    ): Pair<List<Artwork>, List<Episode>> {

        val artworks = arrayListOf<Artwork>()
        val episodes = arrayListOf<Episode>()

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
        artworks: List<Artwork>,
        episodes: List<Episode>
    ) : Pair<List<Artwork>, List<Episode>> {

        val filePaths = files.map { it.path }
        val episodesToRemove = arrayListOf<Episode>()
        val moviesToRemove = arrayListOf<Artwork>()
        val showsToRemove = arrayListOf<Artwork>()

        episodes.forEach {

            if (!filePaths.contains(it.file.path))
                episodesToRemove.add(it)
        }

        artworks.filter { it.content is Content.MOVIE }.forEach {

            if (!filePaths.contains((it.content as Content.MOVIE).movie.file.path))
                moviesToRemove.add(it)

        }

        artworks.filter { it.content is Content.SHOW }.forEach { show ->

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

    override suspend fun saveArtwork(artwork: Artwork) {
        databaseManager.saveArtworks(listOf(artwork))
    }

    override suspend fun saveEpisodes(episodes: List<Episode>) {
        databaseManager.saveEpisodes(episodes)
    }

}