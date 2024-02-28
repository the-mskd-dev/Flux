package com.kaem.flux.data.repository

import com.kaem.flux.data.ddb.DatabaseManager
import com.kaem.flux.model.flux.Episode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class EpisodeRepository @Inject constructor(private val databaseManager: DatabaseManager) {

    private val _episodes = MutableStateFlow<List<Episode>>(emptyList())
    val episodes: StateFlow<List<Episode>> = _episodes.asStateFlow()

    suspend fun fetchEpisodes(showId: Int) {

        _episodes.value = databaseManager.getEpisodesForShow(showId = showId)

    }

}