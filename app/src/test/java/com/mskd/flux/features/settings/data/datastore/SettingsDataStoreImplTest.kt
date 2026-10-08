package com.mskd.flux.features.settings.data.datastore

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import app.cash.turbine.test
import com.mskd.flux.core.model.language.Language
import com.mskd.flux.features.settings.domain.datastore.SettingsDataStore
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import okio.Path.Companion.toPath
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsDataStoreImplTest : FunSpec({

    var tempFile: File? = null
    lateinit var settingsDataStore: SettingsDataStore

    beforeTest {
        tempFile = File.createTempFile("test_datastore_", ".preferences_pb")

        val testDispatcher = UnconfinedTestDispatcher()
        val testScope = TestScope(testDispatcher)

        val testDataStore = PreferenceDataStoreFactory.createWithPath(
            produceFile = { tempFile!!.absolutePath.toPath() },
            scope = testScope
        )

        settingsDataStore = SettingsDataStoreImpl(
            settingsDataStore = testDataStore
        )
    }

    afterTest {
        tempFile?.delete()
        tempFile = null
    }

    test("initial state should match default values") {

        // Given
        val expectedSate = SettingsDataStore.State()
        settingsDataStore.flow.test {

            // When
            val initialState = awaitItem()

            // Then
            initialState shouldBe expectedSate
            cancelAndConsumeRemainingEvents()
        }
    }

    test("setPlayerRewindValue should update value in flow") {

        // Given
        val newValue = 20
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setPlayerRewindValue(newValue)

            // Then
            val updated = awaitItem()
            updated.playerRewindValue shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setPlayerForwardValue should update value in flow") {

        // Given
        val newValue = 20
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setPlayerForwardValue(newValue)

            // Then
            val updated = awaitItem()
            updated.playerForwardValue shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setSubtitlesLanguage should update value in flow") {

        // Given
        val newValue = Language.JAPANESE.code
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setSubtitlesLanguage(newValue)

            // Then
            val updated = awaitItem()
            updated.subtitlesLanguage shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setAudioLanguage should update value in flow") {

        // Given
        val newValue = Language.JAPANESE.code
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setAudioLanguage(newValue)

            // Then
            val updated = awaitItem()
            updated.audioLanguage shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }
    }

    test("setExternalPlayer should update value in flow") {

        // Given
        val newValue = !SettingsDataStore.State().externalPlayer
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setExternalPlayer(newValue)

            // Then
            val updated = awaitItem()
            updated.externalPlayer shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }

    }

    test("setAutoKeyboard should update value in flow") {

        // Given
        val newValue = !SettingsDataStore.State().autoKeyboard
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setAutoKeyboard(newValue)

            // Then
            val updated = awaitItem()
            updated.autoKeyboard shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }

    }

    test("setPrefetchHdImages should update value in flow") {

        // Given
        val newValue = !SettingsDataStore.State().prefetchHdImages
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setPrefetchHdImages(newValue)

            // Then
            val updated = awaitItem()
            updated.prefetchHdImages shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }

    }

    test("setDataLanguage should update value in flow") {

        // Given
        val newValue = Language.KOREAN.code
        settingsDataStore.flow.test {
            awaitItem()

            // When 1 : set a non-null value
            settingsDataStore.setDataLanguage(newValue)

            // Then 1 : test the new value
            val nonNullState = awaitItem()
            nonNullState.dataLanguage shouldBe newValue

            // When 2 : set a null value
            settingsDataStore.setDataLanguage(null)

            // Then 2 : test the null value
            val nullState = awaitItem()
            nullState.dataLanguage shouldBe null

            cancelAndConsumeRemainingEvents()
        }

    }

    test("setSystemFolders should update value in flow") {

        // Given
        val newValue = !SettingsDataStore.State().systemFoldersEnabled
        settingsDataStore.flow.test {
            awaitItem()

            // When
            settingsDataStore.setSystemFolders(newValue)

            // Then
            val updated = awaitItem()
            updated.systemFoldersEnabled shouldBe newValue

            cancelAndConsumeRemainingEvents()
        }

    }

})