package com.mskd.flux.features.identification.domain.usecase

import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.catalog.domain.sync.enrichment.Enrichment
import com.mskd.flux.features.catalog.domain.sync.enrichment.FolderDraft
import com.mskd.flux.report.Trace
import kotlinx.coroutines.CancellationException

/**
 * Manual identification: every Media of an Artwork is re-identified to the Artwork chosen by the user, by replaying the
 * [enrichments] of the Sync under its TMDB ID. The result is marked as manually identified, and is Private when the
 * source or the chosen Artwork is. An Episode that TMDB does not list in a Season of the chosen Show stays in that Show, and so do the Unrecognized
 * Episode of the same file-name title. When the chosen Artwork cannot be fetched, or has none of the files, nothing is saved:
 * the Media stay where they are.
 */
class IdentifyManuallyUseCase(
    private val database: DatabaseRepository,
    private val enrichments: List<Enrichment>,
) {

    private companion object { const val TAG = "IdentifyManuallyUseCase" }

    /** The ID of the Artwork the Media now belong to. */
    suspend operator fun invoke(artworkId: Long, target: Artwork): Result<Long> = try {
        Result.success(identify(artworkId = artworkId, target = target))
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Trace.error(TAG, "Fail to identify artworkId:$artworkId as ${target.type} ${target.id}", e)
        Result.failure(e)
    }

    /** The Unrecognized Episode files with the same (title, year) in their file name as [files], which are not in [files]. */
    private suspend fun unrecognizedSiblings(of: List<UserFile>): List<UserFile> {
        val titleYear = of.map { it.nameProperties.let { properties -> properties.title to properties.year } }.distinct().singleOrNull() ?: return emptyList()
        val known = of.map { it.path }.toSet()
        return database.getEpisodes(artworkId = Artwork.UNKNOWN_ID)
            .map { it.file }
            .filter { it.isEpisode && it.path !in known && it.nameProperties.let { properties -> properties.title to properties.year } == titleYear }
    }

    private suspend fun identify(artworkId: Long, target: Artwork): Long {

        val source = database.getArtwork(artworkId = artworkId) ?: error("No artwork $artworkId")
        check(!source.isUnknown && !target.isUnknown) { "The Unknown Artwork cannot be identified manually" }
        check(source.id != target.id) { "Artwork $artworkId is already the chosen one" }
        check(source.type == target.type) { "Artwork $artworkId is a ${source.type}, not a ${target.type}" }

        val medias = database.getMovie(artworkId = artworkId)?.let { listOf(it) } ?: database.getEpisodes(artworkId = artworkId)
        val sourceFiles = medias.map { it.file }.ifEmpty { error("No file in artwork $artworkId") }
        val properties = sourceFiles.first().nameProperties

        // The automatic identification left Unrecognized the Episode TMDB did not list: they follow the Show of their file name
        val files = if (source.type == ContentType.SHOW) sourceFiles + unrecognizedSiblings(of = sourceFiles) else sourceFiles

        val draft = FolderDraft(folder = CatalogFolder(title = properties.title, year = properties.year, files = files))
        draft.artwork = target
        enrichments.forEach { it.enrich(draft) }

        val artwork = draft.artwork?.takeIf { draft.isSavable && !it.isUnknown } ?: error("No data for ${target.type} ${target.id}")
        // Otherwise the chosen Artwork, left empty, would be deleted by the save
        check(draft.medias.any { it.artworkId == artwork.id }) { "No file of artwork $artworkId in ${target.type} ${target.id}" }

        // The chosen Show has the Season but does not list the Episode: it stays in the Show, with the data of its file name.
        // The Episode of a Season the chosen Show does not have stay Unrecognized: the Show page lists Episode by Season.
        val seasonNumbers = draft.seasons.map { it.season }.toSet()
        val identifiedMedias = draft.medias.map { media ->
            if (media is Episode && media.isUnknown && media.season in seasonNumbers) media.copy(artworkId = artwork.id) else media
        }

        database.saveIdentifiedFolder(
            sourceArtworkId = source.id,
            artwork = artwork.copy(isPrivate = source.isPrivate || artwork.isPrivate, isManuallyIdentified = true),
            seasons = draft.seasons,
            people = draft.people,
            // The primary key of medias is (id, artworkId): never save twice the same media identity
            medias = identifiedMedias.distinctBy { it.mediaId to it.artworkId },
        )

        return artwork.id

    }

}
