package com.mskd.flux.data.repository.catalog

import android.util.Log
import com.mskd.flux.data.ddb.DatabaseDao
import com.mskd.flux.data.source.file.FilesSource
import com.mskd.flux.data.source.media.MediaSource
import com.mskd.flux.data.source.media.MediaSourceTMDBImpl.Companion.TAG
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import io.sentry.Sentry
import kotlinx.coroutines.CoroutineScope
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
    private val db: DatabaseDao,
    scope: CoroutineScope
) : CatalogRepository {

    private val _catalogFlow = MutableStateFlow(CatalogRepository.State())
    override val flow: StateFlow<CatalogRepository.State> = _catalogFlow.asStateFlow()

    private var isSyncing = false

    init {
        scope.launch {

            var medias: List<Artwork> = emptyList()

            try {

                medias = mediaSourceLocal.getMedias().artworks

            } catch (e: Exception) {
                Log.e(TAG, "Fail to init catalog", e)
                Sentry.captureException(e)
            }

            // Update content
            _catalogFlow.update { content ->
                content.copy(
                    isLoading = isSyncing,
                    artworks = medias.sortedBy { it.title }
                )
            }

        }
    }

    override suspend fun syncCatalog() {

        isSyncing = true

        _catalogFlow.update { it.copy(isLoading = true) }

        try {

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

            val allArtworks = db.getArtworks()

            // Update content
            _catalogFlow.update { content ->
                content.copy(
                    isLoading = false,
                    artworks = allArtworks.sortedBy { it.title }
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "[syncCatalog] Fail to sync catalog", e)
            Sentry.captureException(e)

            _catalogFlow.update { content ->
                content.copy(isLoading = false)
            }
        }

        isSyncing = false

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