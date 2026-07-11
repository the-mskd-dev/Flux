package com.mskd.flux.features.catalog.usecase

import com.mskd.flux.configs.fluxExtensions
import com.mskd.flux.features.catalog.domain.coordinator.CatalogSyncCoordinator
import com.mskd.flux.features.catalog.domain.model.SyncState
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope

@OptIn(ExperimentalCoroutinesApi::class)
class CatalogSyncCoordinatorTest : FunSpec({

    fluxExtensions()

    val testDispatcher = StandardTestDispatcher()
    val testScope = TestScope(testDispatcher)

    test("initial state is Idle") {
        val coordinator = CatalogSyncCoordinator(scope = testScope)
        coordinator.state.value shouldBe SyncState.Idle
        coordinator.isBusy shouldBe false
    }

    test("launch changes state to Syncing and back to Idle when done") {
        val coordinator = CatalogSyncCoordinator(scope = testScope)

        coordinator.launch(full = true) {
            delay(100)
        }

        // Job starts, transitions to Syncing
        testDispatcher.scheduler.runCurrent()
        coordinator.state.value.shouldBeInstanceOf<SyncState.Syncing>()
        (coordinator.state.value as SyncState.Syncing).full shouldBe true
        coordinator.isBusy shouldBe true

        // Advance time for delay to finish
        testDispatcher.scheduler.advanceTimeBy(150)
        testDispatcher.scheduler.runCurrent()

        coordinator.state.value shouldBe SyncState.Idle
        coordinator.isBusy shouldBe false
    }

    test("incrementProgress calculates progress correctly") {
        val coordinator = CatalogSyncCoordinator(scope = testScope)
        
        coordinator.launch(full = false) {
            delay(1000)
        }
        testDispatcher.scheduler.runCurrent()

        coordinator.state.value.shouldBeInstanceOf<SyncState.Syncing>()
        (coordinator.state.value as SyncState.Syncing).progress shouldBe 0f

        coordinator.setTotalSteps(4)
        
        coordinator.incrementProgress()
        (coordinator.state.value as SyncState.Syncing).progress shouldBe 0.25f

        coordinator.incrementProgress()
        (coordinator.state.value as SyncState.Syncing).progress shouldBe 0.50f
    }

})
