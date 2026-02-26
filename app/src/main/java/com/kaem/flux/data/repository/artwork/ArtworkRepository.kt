package com.kaem.flux.data.repository.artwork

import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.model.artwork.Artwork
import com.kaem.flux.model.artwork.ContentType
import com.kaem.flux.model.artwork.Episode
import com.kaem.flux.model.artwork.Movie
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

interface ArtworkRepository {

    val flow: Flow<Content>

    fun searchArtwork(mediaId: Long)

    suspend fun saveMovie(movie: Movie)

    suspend fun saveEpisode(episode: Episode)

    suspend fun saveEpisodes(episodes: List<Episode>)

    data class Content(
        val artwork: Artwork? = null,
        val movie: Movie? = null,
        val episodes: List<Episode> = emptyList()
    )

}