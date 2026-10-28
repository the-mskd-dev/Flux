package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.catalog.CatalogFolder

/**
 * Enrichissement: a data fetched for the Artwork of a folder during the catalogue step. Adding a TMDB data
 * means adding an Enrichissement to the chain: neither the Synchro use case nor the progress calculation change.
 */
interface Enrichment {

    /** Estimated units of work for [folder], deduced from its file names. */
    fun units(folder: CatalogFolder): Int

    /** Throws when the folder cannot be fetched: its files are then Fichiers en échec. */
    suspend fun enrich(draft: FolderDraft)

}
