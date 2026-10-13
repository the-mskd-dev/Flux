package com.mskd.flux.features.credits.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.People
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Emits the list of [People] linked to an artwork.
 *
 * The data comes from the local database when available. When the database has no entry
 * for the artwork, the cast is fetched from the TMDB API, then persisted so the database
 * becomes the single source of truth for subsequent emissions.
 */
class GetCreditsUseCase(
    private val creditsRepository: CreditsRepository,
    private val apiRepository: ApiRepository,
    private val databaseRepository: DatabaseRepository,
) {

    operator fun invoke(artworkId: Long): Flow<ImmutableList<People>> {
        return creditsRepository.flowPeople(artworkId = artworkId)
            .onEach { cached ->
                if (cached.isEmpty()) {

                    val type = databaseRepository.getArtwork(artworkId = artworkId)?.type
                        ?: return@onEach

                    val fetched = when (type) {
                        ContentType.MOVIE -> apiRepository.getMovieCredits(artworkId = artworkId)
                        ContentType.SHOW -> apiRepository.getShowCredits(artworkId = artworkId)
                    }

                    if (fetched.isNotEmpty()) {
                        creditsRepository.savePeople(people = fetched)
                    }

                }
            }
            .map { it.toImmutableList() }
            .flowOn(Dispatchers.IO)
    }

}