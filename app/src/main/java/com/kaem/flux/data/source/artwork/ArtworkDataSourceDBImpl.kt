package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.flux.ArtworkOverview

import com.kaem.flux.model.flux.Episode
import com.kaem.flux.model.flux.Movie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
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

            val artworkOverviews = arrayListOf<ArtworkOverview>()
            val movies = arrayListOf<Movie>()
            val episodes = arrayListOf<Episode>()

            coroutineScope {

                launch {

                    val dbArtworks = databaseManager.getArtworks()
                    artworkOverviews.addAll(dbArtworks)

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
                artworkOverviews = artworkOverviews,
                movies = movies,
                episodes = episodes
            )

        } else {

            return ArtworkDataSource.Library(artworkOverviews = databaseManager.getArtworks())

        }

    }

}