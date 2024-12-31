package com.kaem.flux.data.source.artwork

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.ArtworkOverview

import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ArtworkDataSourceDBImpl @Inject constructor(
    private val db: FluxDao
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

            withContext(Dispatchers.IO) {
                coroutineScope {

                    launch {

                        val dbOverviews = db.getOverviews()
                        overviews.addAll(dbOverviews)

                    }

                    launch {

                        val dbMovies = db.getMovies()
                        movies.addAll(dbMovies)

                    }

                    launch {

                        val dbEpisodes = db.getEpisodes()
                        episodes.addAll(dbEpisodes)

                    }

                }
            }

            return ArtworkDataSource.Library(
                overviews = overviews,
                movies = movies,
                episodes = episodes
            )

        } else {

            return ArtworkDataSource.Library(overviews = db.getOverviews())

        }

    }

}