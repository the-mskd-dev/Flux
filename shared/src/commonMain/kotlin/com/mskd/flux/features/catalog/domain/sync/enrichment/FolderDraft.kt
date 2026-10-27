package com.mskd.flux.features.catalog.domain.sync.enrichment

import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.core.model.artwork.Person
import com.mskd.flux.core.model.artwork.Season
import com.mskd.flux.core.model.catalog.CatalogFolder

/** What the Enrichissements have gathered so far for a folder. */
class FolderDraft(val folder: CatalogFolder) {

    var artwork: Artwork? = null

    val seasons = mutableListOf<Season>()

    var medias: List<Media> = emptyList()

    /** Cast and crew of the Artwork, saved with it. */
    var people: List<Person> = emptyList()

    /** Number of Fichiers en échec: not persisted, retried by the next Synchro. */
    var failedFiles = 0

    val isUnknown: Boolean get() = artwork?.isUnknown == true

    /** An Artwork none of whose files could be fetched is not saved either. */
    val isSavable: Boolean
        get() {
            val artwork = artwork ?: return false
            return artwork.isUnknown || seasons.isNotEmpty() || medias.any { it is Movie }
        }

}
