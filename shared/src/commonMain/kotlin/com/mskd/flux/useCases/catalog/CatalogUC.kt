package com.mskd.flux.useCases.catalog

import com.mskd.flux.model.Catalog
import com.mskd.flux.model.UserFile
import com.mskd.flux.model.artwork.Artwork
import kotlinx.coroutines.flow.Flow

/**
 * Use case interface for managing the media catalog.
 */
interface CatalogUC {

    /**
     * Flow representing the current state of catalog synchronization.
     */
    val state: Flow<State>

    /**
     * Flow emitting the list of all artworks saved in the database.
     */
    val artworks : Flow<List<Artwork>>

    /**
     * Synchronizes the local media catalog with files on the device.
     *
     * It scans the media files, queries TMDB for metadata, and updates the database.
     * If [onlyNew] is true, it only processes newly added files.
     */
    fun syncCatalog(onlyNew: Boolean)

    /**
     * Retrieves and constructs a [Catalog] object from a list of user files.
     *
     * It groups files into folders and parallelly fetches artwork metadata.
     */
    suspend fun getCatalog(
        files: List<UserFile>,
        updateProgress: () -> Unit
    ) : Catalog

    /**
     * Cleans up the database catalog by removing entries of files that no longer exist.
     */
    suspend fun cleanCatalog()

    /**
     * Re-fetches localized details (translations) for all items currently in the catalog.
     */
    fun updateLanguage()

    /**
     * Represents the current state of the catalog synchronization.
     */
    sealed class State {
        data object Idle: State()
        data class Syncing(
            val full: Boolean,
            val progress: Float = 0f
        ) : State()
    }

}