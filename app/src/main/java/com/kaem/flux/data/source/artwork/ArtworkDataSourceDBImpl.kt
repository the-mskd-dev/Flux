package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview

import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class ArtworkDataSourceDBImpl @Inject constructor(
    private val databaseManager: DatabaseManager
) : ArtworkDataSource {

    override suspend fun getArtworks(
        files: List<UserFile>,
        overviewIds: List<Long>,
        sync: Boolean
    ): ArtworkDataSource.Library {

        if (sync) {

            val overviews = arrayListOf<ArtworkOverview>()
            val movies = arrayListOf<Movie>()
            val episodes = arrayListOf<Episode>()

            coroutineScope {

                launch {

                    val dbOverviews = databaseManager.getOverviews()
                    overviews.addAll(dbOverviews)

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
                overviews = overviews,
                movies = movies,
                episodes = episodes
            )

        } else {

            return ArtworkDataSource.Library(overviews = databaseManager.getOverviews())

        }

    }

}