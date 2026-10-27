package com.mskd.flux.features.catalog.sync

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.mskd.flux.core.database.data.FluxDatabase
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.moduleDatabase
import com.mskd.flux.core.datastore.InMemoryPreferencesDataStore
import com.mskd.flux.core.datastore.data.UserDataStoreImpl
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.moduleTmdb
import com.mskd.flux.core.network.tmdb.data.TMDB_MAX_REQUESTS
import com.mskd.flux.core.network.tmdb.data.tmdbHttpClient
import com.mskd.flux.di.Qualifiers
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.catalog.fake.FakeCatalogSyncCoordinator
import com.mskd.flux.features.catalog.moduleCatalog
import com.mskd.flux.features.files.domain.datasource.FilesDataSource
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.files.fake.FakeFilesDataSource
import com.mskd.flux.features.files.moduleFiles
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.images.fake.FakeImagesPrefetchManager
import com.mskd.flux.features.people.domain.repository.PeopleRepository
import com.mskd.flux.features.people.modulePeople
import com.mskd.flux.features.privateFolder.data.datastore.PrivateFolderDataStoreImpl
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.settings.data.datastore.SettingsDataStoreImpl
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.sources.domain.provider.SourcesProvider
import com.mskd.flux.features.token.data.datastore.TokenDataStoreImpl
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import io.ktor.client.HttpClient
import io.mockk.mockk
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.koin.dsl.koinApplication
import org.koin.dsl.module
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.time.Duration.Companion.seconds

/**
 * Drives the Synchro through its only entry point, the Synchro use case, with the real Synchro graph.
 * Only the outer dependencies are replaced: in-memory Sources, TMDB simulated at the HTTP level,
 * in-memory Room database and datastores.
 *
 * Observe the result through [catalog], [states] and [tmdb] call counts.
 */
internal class SyncHarness(tmdbToken: String = "test-token", tmdbMaxRequests: Int = TMDB_MAX_REQUESTS) : AutoCloseable {

    companion object {
        const val DEVICE_ROOT = "content://media/external/video/"
        const val FILE_DURATION = 42
    }

    val tmdb = FakeTmdb()

    /** Files whose duration cannot be read from the file. */
    val unreadableDurations = CopyOnWriteArrayList<String>()

    /** The device storage, always available. */
    val deviceStorage = FakeFilesDataSource(availableFiles = emptyList(), root = DEVICE_ROOT)

    private val customSources = CopyOnWriteArrayList<FakeFilesDataSource>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val recorderScope = CoroutineScope(SupervisorJob() + Dispatchers.Unconfined)

    private val _states = CopyOnWriteArrayList<SyncState>()

    /** Successive values of the Synchro state since the harness creation. */
    val states: List<SyncState> get() = _states.toList()

    private val koinApplication = koinApplication {
        allowOverride(true)
        modules(
            moduleDatabase,
            moduleTmdb,
            moduleCatalog,
            moduleFiles,
            modulePeople,
            module {
                single<RoomDatabase.Builder<FluxDatabase>> {
                    Room.inMemoryDatabaseBuilder(mockk<Context>(relaxed = true), FluxDatabase::class.java)
                }
                single<CoroutineScope>(Qualifiers.APPLICATION_SCOPE) { scope }
                single { AppInfo(versionCode = 1, versionName = "test", baseToken = tmdbToken) }
                single<SettingsDataStore> { SettingsDataStoreImpl(settingsDataStore = InMemoryPreferencesDataStore()) }
                single<TokenDataStore> { TokenDataStoreImpl(tokenDataStore = InMemoryPreferencesDataStore(), appInfo = get()) }
                single<UserDataStore> { UserDataStoreImpl(userDataStore = InMemoryPreferencesDataStore(), json = get()) }
                single<PrivateFolderDataStore> { PrivateFolderDataStoreImpl(privateFolderDataStore = InMemoryPreferencesDataStore()) }
                single<ImagesPrefetchManager> { FakeImagesPrefetchManager() }
                single<GetFileDurationUseCase> {
                    object : GetFileDurationUseCase {
                        override suspend fun invoke(file: UserFile): Int {
                            if (file.name in unreadableDurations) error("Cannot read ${file.name}")
                            return FILE_DURATION
                        }
                    }
                }
                single<SourcesProvider> {
                    object : SourcesProvider {
                        override suspend fun getSources(): List<FilesDataSource> = listOf(deviceStorage) + customSources
                    }
                }
                single<HttpClient> { tmdbHttpClient(engine = tmdb.engine, json = get(), tokenDataStore = get(), maxRequests = tmdbMaxRequests) }
                single<CatalogSyncCoordinator> { FakeCatalogSyncCoordinator(scope = scope) }
            }
        )
    }

    private val koin = koinApplication.koin

    private val useCase: SyncCatalogUseCase = koin.get()

    /** The Catalogue, as persisted in the database. */
    val catalog: DatabaseRepository = koin.get()

    val people: PeopleRepository = koin.get()

    val privateFolder: PrivateFolderDataStore = koin.get()

    val settings: SettingsDataStore = koin.get()

    val user: UserDataStore = koin.get()

    init {
        recorderScope.launch { useCase.state.collect { _states += it } }
    }

    /** Adds a custom folder Source rooted at [root]. */
    fun addCustomSource(root: String, files: List<UserFile> = emptyList(), available: Boolean = true): FakeFilesDataSource =
        FakeFilesDataSource(availableFiles = files, available = available, root = root).also { customSources += it }

    /** A video file of the device storage. */
    fun deviceFile(name: String): UserFile =
        UserFile(name = name, path = "$DEVICE_ROOT$name", realPath = "Movies/$name", source = FileSource.LOCAL)

    /** A video file of a custom folder Source rooted at [root]. */
    fun customFile(root: String, name: String): UserFile =
        UserFile(name = name, path = "$root/document/$name", realPath = name, source = FileSource.SAF)

    /** Runs a Synchro légère and waits for its end. */
    suspend fun lightSync() {
        requestLightSync()
        awaitIdle()
    }

    /** Runs a Synchro complète and waits for its end. */
    suspend fun fullSync() {
        requestFullSync()
        awaitIdle()
    }

    fun requestLightSync() = useCase(onlyNew = true)

    fun requestFullSync() = useCase(onlyNew = false)

    /** Waits for the Synchro to end, along with the background work it started. */
    suspend fun awaitIdle() = withTimeout(10.seconds) {
        useCase.state.first { it is SyncState.Idle }
        while (true) {
            val running = scope.coroutineContext.job.children.filter { it.isActive }.toList()
            if (running.isEmpty()) break
            running.joinAll()
        }
    }

    override fun close() {
        scope.cancel()
        recorderScope.cancel()
        koin.get<FluxDatabase>().close()
        koinApplication.close()
    }

}
