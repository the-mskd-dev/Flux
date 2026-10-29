package com.mskd.flux.features.files.domain.model

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.files.domain.datasource.FilesDataSource

/** Files seen when scanning the Sources, and the Sources that could not be read. */
class SourcesScan(
    val files: List<UserFile>,
    val unavailableSources: List<FilesDataSource>,
) {

    /**
     * The files of [knownFiles] that still exist: the ones seen by the scan, plus the ones that belong to a
     * Source indisponible. Any other known file is a Fichier manquant.
     */
    fun existingFiles(knownFiles: List<UserFile>): List<UserFile> =
        files + knownFiles.filter { file -> unavailableSources.any { it.contains(file) } }

}
