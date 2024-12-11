package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.Artwork

import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtworkDataSourceDBImpl @Inject constructor(
    private val databaseManager: DatabaseManager
) : ArtworkDataSource {

    override suspend fun getArtworks(
        files: List<UserFile>,
        artworkIds: List<Long>,
        sync: Boolean
    ): ArtworkDataSource.Library {

        if (sync) {

            val artworks = arrayListOf<Artwork>()
            val movies = arrayListOf<Movie>()
            val episodes = arrayListOf<Episode>()

            coroutineScope {

                launch {

                    val dbArtworks = databaseManager.getArtworks()
                    artworks.addAll(dbArtworks)

                }

                launch {

                    val dbMovies = databaseManager.getMovies()
                    movies.addAll(dbMovies)

                }

                launch {

                    val dbEpisodes = databaseManager.getEpisodes()
                    episodes.addAll(dbEpisodes)

                }

            }

            return ArtworkDataSource.Library(
                artworks = artworks,
                movies = movies,
                episodes = episodes
            )

        } else {

            return ArtworkDataSource.Library(artworks = databaseManager.getArtworks())

        }

    }

}