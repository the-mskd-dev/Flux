package com.kaem.flux.data.repository

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.source.file.FilesSource
import com.kaem.flux.data.source.media.MediaSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.media.Episode
import com.kaem.flux.model.media.MediaOverview
import com.kaem.flux.model.media.Movie
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogContent(
    val isLoading: Boolean = true,
    val mediaOverviews: List<MediaOverview> = emptyList()
)

class CatalogRepository @Inject constructor(
    private val fileSource: FilesSource,
    private val mediaSourceLocal: MediaSource,
    private val mediaSourceTmdb: MediaSource,
    private val db: DatabaseDao
) {

    private val _catalogFlow = MutableStateFlow(CatalogContent())
    val catalogFlow: StateFlow<CatalogContent> = _catalogFlow.asStateFlow()

    suspend fun getCatalog(sync: Boolean = false) {

        _catalogFlow.update { it.copy(isLoading = true) }

        var medias: List<MediaOverview> = emptyList()

        try {

            medias = if (sync)
                syncCatalog()
            else
                mediaSourceLocal.getMedias(sync = false).overviews

        } catch (e: Exception) {
            Firebase.crashlytics.recordException(e)
        }

        // Update content
        _catalogFlow.update { content ->
            content.copy(
                isLoading = false,
                mediaOverviews = medias.sortedBy { it.title }
            )
        }

    }

    private suspend fun syncCatalog() : List<MediaOverview> {

        // Fetch all files, local and online (if possible)
        val allFiles = getFiles()
        val dbFileNames = db.getAllFileNames()

        // Delete medias with missing files
        db.deleteMediasWithNoFiles(allFiles)

        // Get new medias from TMBD
        val newFiles = allFiles.filter { !dbFileNames.contains(it.name) }
        val (newOverviews, newMovies, newEpisodes) = mediaSourceTmdb.getMedias(
            files = newFiles,
            sync = true
        )

        // Save new medias
        db.insertOverviews(newOverviews)
        db.insertMovies(newMovies)
        db.insertEpisodes(newEpisodes)

        // Return all overviews
        return db.getOverviews()

    }

    private suspend fun getFiles() : List<UserFile> {

        val localFiles = arrayListOf<UserFile>()

        coroutineScope {

            launch {
                localFiles.addAll(fileSource.getFiles())
            }

            //TODO: Add other sources

        }

        return localFiles

    }


}