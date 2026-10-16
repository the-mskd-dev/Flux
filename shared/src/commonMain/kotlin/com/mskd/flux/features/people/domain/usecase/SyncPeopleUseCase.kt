package com.mskd.flux.features.people.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.people.domain.repository.PeopleRepository
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

class SyncPeopleUseCase(
    private val databaseRepository: DatabaseRepository,
    private val apiRepository: ApiRepository,
    private val peopleRepository: PeopleRepository,
    private val userDataStore: UserDataStore,
    private val scope: CoroutineScope,
) {

    companion object {
        const val TAG = "SyncPeopleUseCase"
        val SYNC_INTERVAL_MILLIS = 7.days.inWholeMilliseconds
    }

    operator fun invoke(artworks: List<Artwork>? = null, force: Boolean = false) = scope.launch {

        if (artworks != null) {
            syncArtworks(artworks = artworks)
            return@launch
        }

        val now = Clock.System.now().toEpochMilliseconds()
        val lastSync = userDataStore.getPeopleSyncTime()
        val isExpired = (now - lastSync) >= SYNC_INTERVAL_MILLIS

        if (!force && !isExpired) {
            return@launch
        }

        val allArtworks = databaseRepository.getArtworks(includePrivates = true)
        syncArtworks(artworks = allArtworks)
        userDataStore.setPeopleSyncTime(now)

    }

    private suspend fun syncArtworks(artworks: List<Artwork>) {
        val validArtworks = artworks.filter { it.id != Artwork.UNKNOWN_ID }
        val semaphore = Semaphore(permits = 5)

        val people = coroutineScope {

            validArtworks.map { artwork ->

                async {

                    semaphore.withPermit {

                        try {
                            when (artwork.type) {
                                ContentType.MOVIE -> apiRepository.getMoviePeople(artworkId = artwork.id)
                                ContentType.SHOW -> apiRepository.getShowPeople(artworkId = artwork.id)
                            }
                        } catch (e: Exception) {
                            Trace.error(TAG, "Failed to sync people for artwork ${artwork.id}", e)
                            emptyList()
                        }

                    }

                }

            }.awaitAll().flatten()

        }

        if (people.isNotEmpty()) {
            peopleRepository.savePeople(people = people)
        }

    }

}
