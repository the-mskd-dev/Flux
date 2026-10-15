package com.mskd.flux.features.credits.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository
import com.mskd.flux.features.credits.domain.model.PersonDetails
import com.mskd.flux.features.credits.domain.repository.CreditsRepository
import com.mskd.flux.report.Trace
import kotlinx.collections.immutable.toImmutableList

/**
 * Builds the [PersonDetails] of a single person.
 *
 * The local database is always queried first: the artworks linked to the person and the
 * basic [Person] data are enough to render the offline fallback. The TMDB API is then used
 * to enrich the result; when it fails or returns nothing, the enriched fields are left
 * null so the screen only displays the available information.
 */
class GetPersonDetailsUseCase(
    private val creditsRepository: CreditsRepository,
    private val databaseRepository: DatabaseRepository,
    private val apiRepository: ApiRepository,
) {

    suspend operator fun invoke(person: Person): PersonDetails {

        val artworkIds = creditsRepository.getArtworkIdsForPerson(personId = person.id)
        val artworks = databaseRepository.getArtworks(artworkIds = artworkIds)

        val personInfo = try {
            apiRepository.getPersonInfo(personId = person.id)
        } catch (e: Exception) {
            Trace.error("GetPersonDetails", "Fail to get person details for personId:${person.id}", e)
            null
        }

        return PersonDetails(
            id = person.id,
            name = personInfo?.name?.ifBlank { null } ?: person.name,
            profilePath = personInfo?.profilePath ?: person.profilePath,
            birthday = personInfo?.birthday,
            deathday = personInfo?.deathday,
            placeOfBirth = personInfo?.placeOfBirth,
            biography = personInfo?.biography?.ifBlank { null },
            knownForDepartment = personInfo?.knownForDepartment?.ifBlank { null },
            alsoKnownAs = personInfo?.alsoKnownAs ?: emptyList(),
            homepage = personInfo?.homepage,
            imdbId = personInfo?.imdbId,
            artworks = artworks.toImmutableList(),
        )

    }

}