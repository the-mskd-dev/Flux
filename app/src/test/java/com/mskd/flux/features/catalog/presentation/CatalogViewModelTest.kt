package com.mskd.flux.features.catalog.presentation

import app.cash.turbine.test
import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.core.FakeDatabaseRepository
import com.mskd.flux.core.database.domain.repository.DatabaseRepository
import com.mskd.flux.core.database.domain.repository.DetailsRepository
import com.mskd.flux.core.datastore.domain.UserDataStore
import com.mskd.flux.core.model.artwork.Artwork
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.core.AppInfo
import com.mskd.flux.features.catalog.domain.datastore.CatalogDataStore
import com.mskd.flux.features.catalog.domain.model.CatalogSortingMode
import com.mskd.flux.features.catalog.domain.model.CatalogViewMode
import com.mskd.flux.features.catalog.domain.model.SyncState
import com.mskd.flux.features.catalog.domain.usecase.syncCatalog.SyncCatalogUseCase
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.player.domain.usecase.RecordPlaybackResultUseCase
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogViewModelTest : FunSpec({

    //region Set up

    fluxExtensions()

    lateinit var viewModel: CatalogViewModel
    lateinit var syncCatalogUseCase: SyncCatalogUseCase
    lateinit var artworkDb: DatabaseRepository
    lateinit var detailsDb: DetailsRepository
    lateinit var historyDb: HistoryRepository
    lateinit var userDataStore: UserDataStore
    lateinit var tokenDataStore: TokenDataStore
    lateinit var appInfo: AppInfo
    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var recordPlaybackResult: RecordPlaybackResultUseCase

    beforeTest {

        tokenDataStore = mockk(relaxed = true) {
            coEvery { flow } returns MutableStateFlow("token")
        }
        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }

        syncCatalogUseCase = mockk(relaxed = true) {
            every { state } returns MutableStateFlow(SyncState.Idle)
        }
        artworkDb = FakeDatabaseRepository()

        detailsDb = mockk(relaxed = true) {
            every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
        }

        historyDb = mockk(relaxed = true)

        resolvePlaybackAction = mockk(relaxed = true)
        recordPlaybackResult = mockk(relaxed = true)

        appInfo = AppInfo(
            versionCode = 0,
            versionName = "Version-Test"
        )

    }

    fun createViewModel(
        syncUseCase: SyncCatalogUseCase = syncCatalogUseCase,
        catalogDataStore: CatalogDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(CatalogDataStore.State())
        },
    ): CatalogViewModel {
        return CatalogViewModel(
            syncCatalogUseCase = syncUseCase,
            artworkDb = artworkDb,
            detailsDb = detailsDb,
            historyDb = historyDb,
            userDataStore = userDataStore,
            tokenDataStore = tokenDataStore,
            catalogDataStore = catalogDataStore,
            appInfo = appInfo,
            resolvePlaybackAction = resolvePlaybackAction,
            recordPlaybackResult = recordPlaybackResult
        )
    }

    //endregion

    //region Init

    test("Initial state") {

        // Given & When
        viewModel = createViewModel()

        viewModel.uiState.test {

            // Then
            val initialState = awaitItem()
            initialState.state.shouldBeInstanceOf<CatalogState.Content>()

            cancelAndConsumeRemainingEvents()
        }
    }

    //endregion

    //region Sync

    test("Sync - SyncCatalog should call sync for new files") {

        // Given
        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)
        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        // When
        viewModel.handleIntent(CatalogIntent.SyncCatalog)

        // Then
        verify {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }


    test("Sync - should sync when last sync was more than 1 day ago") {

        // Given
        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)
        val oldTime = System.currentTimeMillis() - 2.days.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns oldTime

        // When
        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        // Then
        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = true)
        }

    }

    test("Sync - should sync when last sync was less than 1 day ago (delegated to usecase)") {

        // Given
        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime

        // When
        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        // Then
        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = true)
        }
    }

    test("Sync - should sync when new app version") {

        // Given
        val syncCatalogUseCaseSpy = spyk(syncCatalogUseCase)
        val recentTime = System.currentTimeMillis() - 12.hours.inWholeMilliseconds
        coEvery { userDataStore.getSyncTime() } returns recentTime
        appInfo = AppInfo(
            versionCode = Int.MAX_VALUE,
            versionName = "VersionTest"
        )

        // When
        viewModel = createViewModel(syncUseCase = syncCatalogUseCaseSpy)

        // Then
        verify(exactly = 1) {
            syncCatalogUseCaseSpy(onlyNew = false)
        }
    }

    //endregion

    //region Navigation

    test("OnArtworkTap - should send NavigateToShow event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = MediaMockups.showArtwork, rgb = 0x112233))

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = 0x112233)
        }

    }

    test("OnArtworkTap - should send NavigateToMovie event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = MediaMockups.movieArtwork, rgb = 0x112233))

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = 0x112233)
        }

    }

    test("OnArtworkTap - should send NavigateToUnknown event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnArtworkTap(artwork = Artwork.UNKNOWN, rgb = null))

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToUnknown
        }

    }

    test("OnCategoryTap - should send NavigateToSearch event with a given type") {

        checkAll(
            Exhaustive.enum<ContentType>()
        ) { type ->

            // Given
            viewModel = createViewModel()

            viewModel.event.test {

                // When
                viewModel.handleIntent(CatalogIntent.OnCategoryTap(category = type))

                // Then
                awaitItem() shouldBe CatalogEvent.NavigateToSearch(category = type)
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("OnGenreTap - should send NavigateToSearch event with a given genre") {

        checkAll(
            iterations = 30,
            Arb.element(DetailsMockup.allGenres)
        ) { genre ->

            // Given
            viewModel = createViewModel()

            viewModel.event.test {

                // When
                viewModel.handleIntent(CatalogIntent.OnGenreTap(genre = genre))

                // Then
                awaitItem() shouldBe CatalogEvent.NavigateToSearch(genre = genre)
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("OnSearchTap - should send NavigateToSearch event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnSearchTap)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToSearch()
        }

    }

    test("OnSettingsTap - should send NavigateToSettings event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnSettingsTap)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToSettings
        }
    }

    test("OnHowToTap - should send NavigateToHowTo event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnHowToTap)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToHowTo
        }

    }

    //endregion

    //region View & Sort

    test("ShowSortingModes - open sorting modes bottom sheet") {

        // Given
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(CatalogIntent.ShowSortingModes(show = true))

            // Then
            val state = awaitItem().state
            state.shouldBeInstanceOf<CatalogState.Content>()
            state.showSortingSheet shouldBe true

        }

    }

    test("SelectSortingMode - select and save the selected sorting option") {

        checkAll(
            Exhaustive.enum<CatalogSortingMode>()
        ) { mode ->

            // Given
            val catalogDataStore = mockk<CatalogDataStore>(relaxed = true)
            viewModel = createViewModel(catalogDataStore = catalogDataStore)

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CatalogIntent.SelectSortingMode(mode))

                // Then
                coEvery { catalogDataStore.setSortingMode(mode) }
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    test("ShowViewModes - open sorting modes bottom sheet") {

        // Given
        viewModel = createViewModel()
        viewModel.uiState.test {
            awaitItem()

            // When
            viewModel.handleIntent(CatalogIntent.ShowViewModes(show = true))

            // Then
            val state = awaitItem().state
            state.shouldBeInstanceOf<CatalogState.Content>()
            state.showViewSheet shouldBe true

        }

    }

    test("SelectViewMode - select and save the selected view option") {

        checkAll(
            Exhaustive.enum<CatalogViewMode>()
        ) { mode ->

            // Given
            val catalogDataStore = mockk<CatalogDataStore>(relaxed = true)
            viewModel = createViewModel(catalogDataStore = catalogDataStore)

            viewModel.uiState.test {
                awaitItem()

                // When
                viewModel.handleIntent(CatalogIntent.SelectViewMode(mode))

                // Then
                coEvery { catalogDataStore.setViewMode(mode) }
                cancelAndIgnoreRemainingEvents()
            }

        }

    }

    //endregion

    //region Player

    test("PlayMedia - ") {


        // Given

    }

    test("OnExternalPlayerResult - should call recordPlaybackResult") {

        checkAll(
            iterations = 20,
            Arb.element(MediaMockups.allMedias),
            Arb.long()
        ) { media, progress ->

            // Given
            viewModel = createViewModel()
            viewModel.handleIntent(intent = CatalogIntent.PlayMedia(media = media, forceInternal = true))

            // When
            viewModel.handleIntent(intent = CatalogIntent.OnExternalPlayerResult(progress = progress))

            // Then
            coEvery { recordPlaybackResult(media = media, progress = progress) }

        }

    }

    //endregion

})