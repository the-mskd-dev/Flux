package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository

/**
 * TMDB details of the Artwork of a folder, with its translations and credits in a single call.
 * The title and description in the user language, completed by English, replace those of the search.
 * No details is a failure.
 */
class DetailsEnrichment(private val api: ApiRepository) : Enrichment {

    override fun units(folder: CatalogFolder) = 1

    override suspend fun enrich(draft: FolderDraft) {

        val artwork = draft.artwork?.takeUnless { it.isUnknown } ?: return

        val details = api.getDetails(artworkId = artwork.id, type = artwork.type, file = draft.folder.files.first())
            ?: error("No details for ${artwork.type} ${artwork.id}")

        draft.artwork = details.artwork
        draft.people = details.people
        details.movie?.let { draft.medias = listOf(it) }

    }

}
