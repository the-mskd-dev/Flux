package com.kaem.flux.data.repository.catalog

import android.util.Log
import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.source.file.FilesSource
import com.kaem.flux.data.source.media.MediaSource
import com.kaem.flux.data.source.media.MediaSourceTMDBImpl.Companion.TAG
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.Artwork
import io.sentry.Sentry
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

class CatalogRepositoryImpl @Inject constructor(
    private val fileSource: FilesSource,
    private val mediaSourceLocal: MediaSource,
    private val mediaSourceTmdb: MediaSource,
    private val db: DatabaseDao
) : CatalogRepository {

    private val _catalogFlow = MutableStateFlow(CatalogRepository.Content())
    override val flow: StateFlow<CatalogRepository.Content> = _catalogFlow.asStateFlow()

    override suspend fun loadCatalog(sync: Boolean) {

        _catalogFlow.update { it.copy(isLoading = true) }

        var medias: List<Artwork> = emptyList()

        try {

            medias = if (sync)
                getCatalog()
            else
                mediaSourceLocal.getMedias().artworks

        } catch (e: Exception) {
            Log.e(TAG, "[getCatalog] Fail to get catalog", e)
            Sentry.captureException(e)
        }

        // Update content
        _catalogFlow.update { content ->
            content.copy(
                isLoading = false,
                artworks = medias.sortedBy { it.title }
            )
        }

    }

    override suspend fun getCatalog() : List<Artwork> {

        // Fetch all files, local and online (if possible)
        val allFiles = getFiles()
        val dbFileNames = db.getAllFileNames()

        // Delete medias with missing files
        db.deleteMediasWithNoFiles(allFiles)

        // Get new medias from TMBD
        val newFiles = allFiles.filter { !dbFileNames.contains(it.name) }
        val (newArtworks, newMovies, newEpisodes) = mediaSourceTmdb.getMedias(files = newFiles)

        // Save new medias
        db.insertArtworks(newArtworks)
        db.insertMovies(newMovies)
        db.insertEpisodes(newEpisodes)

        // Return all overviews
        return db.getArtworks()

    }

    override suspend fun getFiles() : List<UserFile> {

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