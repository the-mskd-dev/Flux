package com.kaem.flux.data.source.media

import com.kaem.flux.data.ddb.FluxDao
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MediaSourceDBImpl @Inject constructor(
    private val db: FluxDao
) : MediaSource {

    override suspend fun getMedias(
        files: List<UserFile>,
        sync: Boolean
    ): MediaSource.Library {

        if (sync) {

            val overviews = arrayListOf<MediaOverview>()
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

            return MediaSource.Library(
                overviews = overviews,
                movies = movies,
                episodes = episodes
            )

        } else {

            return MediaSource.Library(overviews = db.getOverviews())

        }

    }

}