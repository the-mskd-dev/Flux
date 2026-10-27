package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.network.tmdb.domain.repository.ApiRepository

/** TMDB details of the movie of a folder. No details is a failure. */
class DetailsEnrichment(private val api: ApiRepository) : Enrichment {

    override fun units(folder: CatalogFolder) = if (folder.files.first().isEpisode) 0 else 1

    override suspend fun enrich(draft: FolderDraft) {

        val artwork = draft.artwork?.takeUnless { it.isUnknown || it.type != ContentType.MOVIE } ?: return

        val movie = api.getMovie(artworkId = artwork.id, file = draft.folder.files.first())
            ?: error("No details for movie ${artwork.id}")

        draft.medias = listOf(movie)

    }

}
