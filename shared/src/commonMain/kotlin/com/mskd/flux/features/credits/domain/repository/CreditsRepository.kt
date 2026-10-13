package com.mskd.flux.features.credits.domain.repository

import com.mskd.flux.core.model.artwork.People
import kotlinx.coroutines.flow.Flow

interface CreditsRepository {

    fun flowPeople(artworkId: Long): Flow<List<People>>

    suspend fun savePeople(people: List<People>)

}