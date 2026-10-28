package com.mskd.flux.features.catalog.domain.sync

import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.model.SourcesScan
import com.mskd.flux.utils.extensions.groupInFolders

/** What the Étapes de synchro share during a Synchro. */
class SyncContext(
    /** True for a Synchro complète: every file is fetched again, the genres are refreshed. */
    val full: Boolean,
    val progress: SyncProgress,
) {

    /** Set by the scan step. */
    lateinit var scan: SourcesScan

    /** The files known by the Catalogue before the Synchro. */
    var knownFiles: List<UserFile> = emptyList()

    /** The files sent to the catalogue step: the new ones and the Fichiers en échec, or all of them when [full]. */
    var filesToFetch: List<UserFile> = emptyList()

    val foldersToFetch: List<CatalogFolder> get() = filesToFetch.groupInFolders()

    /** Number of Fichiers en échec of the catalogue step. */
    var failedFiles: Int = 0

}
