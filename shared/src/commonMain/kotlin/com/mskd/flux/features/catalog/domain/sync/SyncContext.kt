package com.mskd.flux.features.catalog.domain.sync

import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.catalog.domain.model.SyncType
import com.mskd.flux.features.files.domain.model.SourcesScan
import com.mskd.flux.utils.extensions.groupInFolders

/** What the Sync steps share during a Sync. */
class SyncContext(
    /** Type of Sync running: Light sync, Full sync or Language change. */
    val type: SyncType,
    val progress: SyncProgress,
) {

    /** Set by the scan step when enabled. */
    lateinit var scan: SourcesScan

    /** The files known by the Catalog before the Sync. */
    var knownFiles: List<UserFile> = emptyList()

    /** The files sent to the catalog step: the new ones and the Failed files, or all of them for a Full sync. */
    var filesToFetch: List<UserFile> = emptyList()

    val foldersToFetch: List<CatalogFolder> get() = filesToFetch.groupInFolders()

    /** Number of Failed files of the catalog step. */
    var failedFiles: Int = 0

}
