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
import com.mskd.flux.features.history.domain.model.HistoryEntry
import com.mskd.flux.features.history.domain.repository.HistoryRepository
import com.mskd.flux.features.history.domain.usecase.GetHistoryUseCase
import com.mskd.flux.features.player.domain.model.PlaybackAction
import com.mskd.flux.features.player.domain.usecase.ResolvePlaybackActionUseCase
import com.mskd.flux.features.privateFolder.domain.datastore.PrivateFolderDataStore
import com.mskd.flux.features.privateFolder.domain.usecase.setArtworkPrivacy.SetArtworkPrivacyUseCase
import com.mskd.flux.features.progress.domain.usecase.SaveProgressUseCase
import com.mskd.flux.features.token.domain.datastore.TokenDataStore
import com.mskd.flux.mockups.DetailsMockup
import com.mskd.flux.mockups.MediaMockups
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.element
import io.kotest.property.arbitrary.long
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.boolean
import io.kotest.property.exhaustive.enum
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.spyk
import io.mockk.verify
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    lateinit var privateFolderDataStore: PrivateFolderDataStore
    lateinit var appInfo: AppInfo
    lateinit var getHistoryUseCase: GetHistoryUseCase
    lateinit var resolvePlaybackAction: ResolvePlaybackActionUseCase
    lateinit var recordPlaybackResult: SaveProgressUseCase
    lateinit var setArtworkPrivacy: SetArtworkPrivacyUseCase

    beforeTest {

        tokenDataStore = mockk(relaxed = true) {
            coEvery { flow } returns MutableStateFlow("token")
        }
        userDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(UserDataStore.State())
        }
        privateFolderDataStore = mockk(relaxed = true) {
            every { flow } returns MutableStateFlow(PrivateFolderDataStore.State(enabled = true))
        }

        syncCatalogUseCase = mockk(relaxed = true) {
            every { state } returns MutableStateFlow(SyncState.Idle)
        }
        artworkDb = FakeDatabaseRepository()

        detailsDb = mockk(relaxed = true) {
            every { flowGenres() } returns MutableStateFlow(DetailsMockup.allGenres)
        }

        historyDb = mockk(relaxed = true)

        getHistoryUseCase = mockk<GetHistoryUseCase>(relaxed = true)
        every { getHistoryUseCase() } returns MutableStateFlow(persistentListOf())

        resolvePlaybackAction = mockk(relaxed = true)
        recordPlaybackResult = mockk(relaxed = true)
        setArtworkPrivacy = mockk(relaxed = true)

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
            artworkDb = artworkDb,
            detailsDb = detailsDb,
            historyDb = historyDb,
            userDataStore = userDataStore,
            tokenDataStore = tokenDataStore,
            catalogDataStore = catalogDataStore,
            privateFolderDataStore = privateFolderDataStore,
            appInfo = appInfo,
            syncCatalogUseCase = syncUseCase,
            getHistoryUseCase = getHistoryUseCase,
            resolvePlaybackAction = resolvePlaybackAction,
            recordPlaybackResult = recordPlaybackResult,
            setArtworkPrivacy = setArtworkPrivacy
        )
    }

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

    test("OnArtworkTap - should send NavigateToShow event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnArtworkClick(artwork = MediaMockups.showArtwork, rgb = 0x112233))

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToShow(artworkId = MediaMockups.showArtwork.id, rgb = 0x112233)
        }

    }

    test("OnArtworkTap - should send NavigateToMovie event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnArtworkClick(artwork = MediaMockups.movieArtwork, rgb = 0x112233))

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToMovie(artworkId = MediaMockups.movieArtwork.id, rgb = 0x112233)
        }

    }

    test("OnArtworkTap - should send NavigateToUnknown event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnArtworkClick(artwork = Artwork.UNKNOWN, rgb = null))

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
                viewModel.handleIntent(CatalogIntent.OnCategoryClick(category = type))

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
                viewModel.handleIntent(CatalogIntent.OnGenreClick(genre = genre))

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
            viewModel.handleIntent(CatalogIntent.OnSearchClick)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToSearch()
        }

    }

    test("OnSettingsTap - should send NavigateToSettings event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnSettingsClick)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToSettings
        }
    }

    test("OnHowToTap - should send NavigateToHowTo event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnHowToClick)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToHowTo
        }

    }

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

    test("DeleteHistoryEntry - should delete history entry in database") {

        // Given
        val entry = HistoryEntry(
            media = MediaMockups.episode1,
            timestamp = 0L,
            title = "Test"
        )
        val viewModel = createViewModel()

        // When
        viewModel.handleIntent(intent = CatalogIntent.DeleteHistoryEntry(entry = entry))

        // Then
        coVerify { historyDb.delete(entry.media.artworkId) }

    }

    test("ShowDetails - should navigate to artwork details") {

        checkAll(
            iterations = 20,
            Arb.element(MediaMockups.allMedias),
        ) { media ->

            // Given
            viewModel = createViewModel()
            val matchingArtwork = artworkDb.flowArtworks().firstOrNull()
                ?.find { it.id == media.artworkId }

            viewModel.event.test {

                // When
                viewModel.handleIntent(CatalogIntent.ShowDetails(media = media))

                // Then
                if (matchingArtwork == null) {
                    expectNoEvents()
                } else {
                    val expectedEvent = when {
                        matchingArtwork.id == Artwork.UNKNOWN_ID -> CatalogEvent.NavigateToUnknown
                        matchingArtwork.type == ContentType.SHOW -> CatalogEvent.NavigateToShow(
                            artworkId = matchingArtwork.id,
                            rgb = null
                        )
                        else -> CatalogEvent.NavigateToMovie(
                            artworkId = matchingArtwork.id,
                            rgb = null
                        )
                    }
                    awaitItem() shouldBe expectedEvent
                }

                cancelAndIgnoreRemainingEvents()
            }
        }

    }

    test("ShowDetails - should not navigate when artwork is not found") {

        // Given
        val orphanMedia = MediaMockups.episode1.copy(artworkId = -1L)
        viewModel = createViewModel()

        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.ShowDetails(media = orphanMedia))

            // Then
            expectNoEvents()
        }

    }

    test("PlayMedia - should call resolvePlaybackAction and then launch player event") {

        checkAll(
            iterations = 20,
            Arb.element(MediaMockups.allMedias),
            Exhaustive.boolean(),
            Exhaustive.boolean(),
        ) { media, forceInternal, externalPlayerRequested ->

            // Given
            val externalPlayer = !forceInternal && externalPlayerRequested
            resolvePlaybackAction = mockk(relaxed = true) {
                coEvery { invoke(media = media, forceInternal = forceInternal) } returns PlaybackAction.OpenPlayer(media = media, externalPlayer = externalPlayer)
            }
            viewModel = createViewModel()
            viewModel.event.test {

                // When
                viewModel.handleIntent(intent = CatalogIntent.PlayMedia(media = media, forceInternal = forceInternal))

                // Then
                val event = awaitItem()
                event.shouldBeInstanceOf<CatalogEvent.PlayMedia>()
                event.media shouldBe media
                event.externalPlayer shouldBe externalPlayer

                cancelAndConsumeRemainingEvents()

            }


        }

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

    test("OnPrivateFolderTap - should send NavigateToPrivateFolder event") {

        // Given
        viewModel = createViewModel()
        viewModel.event.test {

            // When
            viewModel.handleIntent(CatalogIntent.OnPrivateFolderClick)

            // Then
            awaitItem() shouldBe CatalogEvent.NavigateToPrivateFolder

        }

    }

    test("AddArtworkToPrivateFolder - should mark artwork as private") {

        // Given
        val artwork = MediaMockups.movieArtwork
        viewModel = createViewModel()

        // When
        viewModel.handleIntent(intent = CatalogIntent.AddArtworkToPrivateFolder(artwork = artwork))

        // Then
        coVerify { setArtworkPrivacy(artworkId = artwork.id, isPrivate = true) }

    }

})