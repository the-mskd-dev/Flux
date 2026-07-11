package com.mskd.flux.features.catalog.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.core.network.tmdb.data.datasource.TmdbDataSource
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCaseImpl
import com.mskd.flux.features.files.domain.usecase.FilterExistingFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetDeviceFilesUseCase
import com.mskd.flux.features.files.domain.usecase.GetFileDurationUseCase
import com.mskd.flux.features.images.domain.ImagesPrefetchManager
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import com.mskd.flux.features.sources.domain.usecase.DeleteUnavailableSourcesUseCase
import io.kotest.core.spec.style.FunSpec
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
class SyncCatalogUseCaseTest : FunSpec({

    fluxExtensions()

    lateinit var tmdb: TmdbDataSource
    lateinit var database: DatabaseRepository
    lateinit var user: UserDataStore
    lateinit var settings: SettingsDataStore
    lateinit var imagesPrefetchManager: ImagesPrefetchManager
    lateinit var appInfo: AppInfo
    lateinit var coordinator: CatalogSyncCoordinator
    lateinit var deleteUnavailableSourcesUseCase: DeleteUnavailableSourcesUseCase
    lateinit var getFileDurationUseCase: GetFileDurationUseCase
    lateinit var getDeviceFilesUseCase: GetDeviceFilesUseCase
    lateinit var filterExistingFilesUseCase: FilterExistingFilesUseCase
    lateinit var useCase: SyncCatalogUseCaseImpl

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    beforeTest {
        tmdb = mockk(relaxed = true)
        database = mockk(relaxed = true)
        user = mockk(relaxed = true)
        settings = mockk(relaxed = true)
        imagesPrefetchManager = mockk(relaxed = true)
        appInfo = AppInfo(versionCode = 1, versionName = "1.0")
        coordinator = CatalogSyncCoordinator(scope = testScope)
        deleteUnavailableSourcesUseCase = mockk(relaxed = true)
        getFileDurationUseCase = mockk(relaxed = true)
        getDeviceFilesUseCase = mockk(relaxed = true)
        filterExistingFilesUseCase = mockk(relaxed = true)

        useCase = SyncCatalogUseCaseImpl(
            tmdb = tmdb,
            database = database,
            user = user,
            settings = settings,
            imagesPrefetchManager = imagesPrefetchManager,
            appInfo = appInfo,
            coordinator = coordinator,
            deleteUnavailableSourcesUseCase = deleteUnavailableSourcesUseCase,
            getFileDurationUseCase = getFileDurationUseCase,
            getDeviceFilesUseCase = getDeviceFilesUseCase,
            filterExistingFilesUseCase = filterExistingFilesUseCase,
            dispatcher = testDispatcher
        )
    }

    test("sync catalog clean and returns early when no new files") {
        coEvery { getDeviceFilesUseCase() } returns emptyList()
        coEvery { database.getMovies() } returns emptyList()
        coEvery { database.getEpisodes() } returns emptyList()
        coEvery { filterExistingFilesUseCase(any()) } returns emptyList()

        useCase(onlyNew = true)
        testDispatcher.scheduler.advanceUntilIdle()

        coVerify(exactly = 1) { deleteUnavailableSourcesUseCase() }
        coVerify(exactly = 1) { database.deleteMediasNotInFiles(emptyList()) }
        coVerify(exactly = 1) { user.setSyncTime(any()) }
        coVerify(exactly = 0) { database.deleteAll() }
        coVerify(exactly = 0) { imagesPrefetchManager.prefetchImages() }
    }

})
