package com.mskd.flux.features.customization.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.customization.domain.model.NavigationStyle
import com.mskd.flux.utils.UiCommon
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.Path.Companion.toPath
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CustomizationDataStoreImplTest : FunSpec({

    var tempFile: File? = null
    lateinit var customizationDataStore: CustomizationDataStore

    beforeTest {
        tempFile = File.createTempFile("test_datastore_", ".preferences_pb")

        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        val testDataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempFile!!.absolutePath.toPath() },
            scope = testScope
        )

        customizationDataStore = CustomizationDataStoreImpl(
            customizationDataStore = testDataStore
        )
    }

    afterTest {
        tempFile?.delete()
        tempFile = null
    }

    test("initial state should match default values") {

        // Given
        val expectedSate = CustomizationDataStore.State()
        customizationDataStore.flow.test {

            // When
            val initialState = awaitItem()

            // Then
            initialState shouldBe expectedSate
            cancelAndConsumeRemainingEvents()
        }

    }

    test("setUiTheme should update value in flow") {

        // Given
        val newValue = UiCommon.THEME.DARK
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setUiTheme(newValue)

            // Then
            val updated = awaitItem()
            updated.uiTheme shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setColor should update value in flow") {

        // Given
        val newValue = 0xFF00FF00.toInt()
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setColor(newValue)

            // Then
            val updated = awaitItem()
            updated.color shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setWaveProgress should update value in flow") {

        // Given
        val newValue = !CustomizationDataStore.State().waveProgress
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setWaveProgress(newValue)

            // Then
            val updated = awaitItem()
            updated.waveProgress shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setOldBlurredHeader should update value in flow") {

        // Given
        val newValue = !CustomizationDataStore.State().oldBlurredHeader
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setOldBlurredHeader(newValue)

            // Then
            val updated = awaitItem()
            updated.oldBlurredHeader shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setLargeEpisodeImage should update value in flow") {

        // Given
        val newValue = !CustomizationDataStore.State().largeEpisodeImage
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setLargeEpisodeImage(newValue)

            // Then
            val updated = awaitItem()
            updated.largeEpisodeImage shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setItemsPerRow should update value in flow") {

        // Given
        val newValue = CustomizationDataStore.State().itemsPerRow + 2
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setItemsPerRow(newValue)

            // Then
            val updated = awaitItem()
            updated.itemsPerRow shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setNavigationStyle should update value in flow") {

        // Given
        val newValue = NavigationStyle.entries
            .filter { it != CustomizationDataStore.State().navigationStyle }
            .random()
        customizationDataStore.flow.test {
            awaitItem()

            // When
            customizationDataStore.setNavigationStyle(newValue)

            // Then
            val updated = awaitItem()
            updated.navigationStyle shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }


})