package com.kaem.flux.data.repository.catalog

import com.google.firebase.Firebase
import com.google.firebase.crashlytics.crashlytics
import com.kaem.flux.data.ddb.DatabaseDao
import com.kaem.flux.data.source.file.FilesSource
import com.kaem.flux.data.source.media.MediaSource
import com.kaem.flux.model.UserFile
import com.kaem.flux.model.artwork.Artwork
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject



interface CatalogRepository {

    val catalogFlow: StateFlow<Content>

    suspend fun getCatalog(sync: Boolean = false)

    suspend fun syncCatalog() : List<Artwork>

    suspend fun getFiles() : List<UserFile>

    data class Content(
        val isLoading: Boolean = true,
        val artworks: List<Artwork> = emptyList()
    )

}