package com.mskd.flux.features.progress

import android.content.Context
import androidx.room.Room
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.data.getRoomDatabase
import com.mskd.flux.core.database.data.repository.DatabaseRepositoryImpl
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.Episode
import com.mskd.flux.core.model.artwork.Media
import com.mskd.flux.core.model.artwork.Movie
import com.mskd.flux.features.history.data.dao.HistoryDao
import com.mskd.flux.features.history.data.model.HistoryEntity
import com.mskd.flux.features.history.data.repository.HistoryRepositoryImpl
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.history.domain.usecase.GetHistoryUseCase
import com.mskd.flux.features.player.domain.model.VideoChapter
import com.mskd.flux.features.player.domain.usecase.GetChaptersUseCase
import com.mskd.flux.features.progress.domain.ProgressTracker
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.repository.SourcesRepository
import com.mskd.flux.features.sources.domain.usecase.FlowSourcesUseCase
import com.mskd.flux.mockups.MediaMockups
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Runs [ProgressTracker] against a real in-memory Room database (`DatabaseRepositoryImpl` +
 * `HistoryRepositoryImpl`), replacing only [GetChaptersUseCase] and [SourcesRepository].
 */
internal class ProgressHarness : AutoCloseable {

    private val fluxDatabase: FluxDatabase = getRoomDatabase(
        builder = Room.inMemoryDatabaseBuilder(mockk<Context>(relaxed = true), FluxDatabase::class.java)
    )

    val database: DatabaseRepository = DatabaseRepositoryImpl(database = fluxDatabase)

    private val historyDao: HistoryDao = fluxDatabase.historyDao()

    val history: HistoryRepository = HistoryRepositoryImpl(dao = historyDao)

    var chapters: List<VideoChapter> = emptyList()

    var onGetChapters: ((Media) -> List<VideoChapter>)? = null

    private val fakeGetChapters = object : GetChaptersUseCase {
        override suspend fun invoke(media: Media): List<VideoChapter> =
            onGetChapters?.invoke(media) ?: chapters
    }

    private val foldersFlow = MutableStateFlow<List<UserFolder>>(emptyList())

    var folders: List<UserFolder>
        get() = foldersFlow.value
        set(value) {
            foldersFlow.value = value
        }

    private val sourcesRepository = object : SourcesRepository {
        override fun flowFolders(): Flow<List<UserFolder>> = foldersFlow.asStateFlow()
        override suspend fun saveFolder(folder: UserFolder) { foldersFlow.value += folder }
        override suspend fun getFolders(): List<UserFolder> = foldersFlow.value
        override suspend fun deleteFolder(folder: UserFolder, deleteMedias: Boolean) {
            foldersFlow.value = foldersFlow.value.filterNot { it.path == folder.path }
        }
        override suspend fun deleteFolders(folders: List<UserFolder>, deleteMedias: Boolean) {
            val paths = folders.mapTo(HashSet()) { it.path }
            foldersFlow.value = foldersFlow.value.filterNot { it.path in paths }
        }
    }

    val getHistory: GetHistoryUseCase = GetHistoryUseCase(
        repository = history,
        sourcesUseCase = FlowSourcesUseCase(sourcesRepository),
    )

    val tracker: ProgressTracker = ProgressTracker(
        database = database,
        history = history,
        getChapters = fakeGetChapters,
        sources = sourcesRepository,
    )

    suspend fun seedMovie(
        artwork: Artwork = MediaMockups.movieArtwork,
        movie: Movie = MediaMockups.movie,
    ) {
        database.saveArtworks(listOf(artwork))
        database.saveMedias(listOf(movie))
    }

    suspend fun seedShow(
        artwork: Artwork = MediaMockups.showArtwork,
        episodes: List<Episode> = MediaMockups.episodes,
    ) {
        database.saveArtworks(listOf(artwork))
        database.saveMedias(episodes)
    }

    suspend fun seedUnknown(episodes: List<Episode> = MediaMockups.unknowns) {
        database.saveArtworks(listOf(Artwork.UNKNOWN))
        database.saveMedias(episodes)
    }

    suspend fun historyEntries(): List<HistoryEntity> = historyDao.getAll()

    override fun close() {
        fluxDatabase.close()
    }

}
