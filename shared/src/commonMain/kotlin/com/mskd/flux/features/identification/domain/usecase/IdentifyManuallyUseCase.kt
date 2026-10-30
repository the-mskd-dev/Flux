package com.mskd.flux.features.identification.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.features.catalog.domain.sync.enrichment.Enrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.FolderDraft
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/**
 * Identification manuelle: every Média of an Artwork is re-identified to the Artwork chosen by the user, by replaying the
 * [enrichments] of the Synchro under its TMDB ID. The result is marked as manually identified, and is Private when the
 * source or the chosen Artwork is. When the chosen Artwork cannot be fetched, nothing is saved: the Médias stay where they are.
 */
class IdentifyManuallyUseCase(
    private val database: DatabaseRepository,
    private val enrichments: List<Enrichment>,
) {

    private companion object { const val TAG = "IdentifyManuallyUseCase" }

    /** The ID of the Artwork the Médias now belong to. */
    suspend operator fun invoke(artworkId: Long, target: Artwork): Result<Long> = try {
        Result.success(identify(artworkId = artworkId, target = target))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Trace.error(TAG, "Fail to identify artworkId:$artworkId as ${target.type} ${target.id}", e)
        Result.failure(e)
    }

    private suspend fun identify(artworkId: Long, target: Artwork): Long {

        val source = database.getArtwork(artworkId = artworkId) ?: error("No artwork $artworkId")
        check(!source.isUnknown && !target.isUnknown) { "The Unknown Artwork cannot be identified manually" }
        check(source.id != target.id) { "Artwork $artworkId is already the chosen one" }
        check(source.type == target.type) { "Artwork $artworkId is a ${source.type}, not a ${target.type}" }

        val medias = database.getMovie(artworkId = artworkId)?.let { listOf(it) } ?: database.getEpisodes(artworkId = artworkId)
        val files = medias.map { it.file }.ifEmpty { error("No file in artwork $artworkId") }
        val properties = files.first().nameProperties

        val draft = FolderDraft(folder = CatalogFolder(title = properties.title, year = properties.year, files = files))
        draft.artwork = target
        enrichments.forEach { it.enrich(draft) }

        val artwork = draft.artwork?.takeIf { draft.isSavable && !it.isUnknown } ?: error("No data for ${target.type} ${target.id}")

        database.saveIdentifiedFolder(
            sourceArtworkId = source.id,
            artwork = artwork.copy(isPrivate = source.isPrivate || artwork.isPrivate, isManuallyIdentified = true),
            seasons = draft.seasons,
            people = draft.people,
            // The primary key of medias is (id, artworkId): never save twice the same media identity
            medias = draft.medias.distinctBy { it.mediaId to it.artworkId },
        )

        return artwork.id

    }

}
