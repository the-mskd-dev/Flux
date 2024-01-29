package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.FileSource
import com.kaem.flux.model.flux.FluxArtworkSummary
import com.kaem.flux.model.flux.FluxEpisode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DatabaseDataSource @Inject constructor(
    private val databaseManager: DatabaseManager
) : ArtworkDataSource {

    override suspend fun getArtworks(
        files: List<FileSource>,
        artworkIds: List<Int>
    ): Pair<List<FluxArtworkSummary>, List<FluxEpisode>> {

        val artworks = arrayListOf<FluxArtworkSummary>()
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

        return Pair(artworks, episodes)

    }

}