package com.mskd.flux.system

import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.provider.DocumentsContract
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidFilesLauncherTest {

    private val recordedIntents = mutableListOf<Intent>()
    private val recordedErrors = mutableListOf<Throwable>()
    private var routeDirToTestExplorer = true
    private lateinit var launcher: AndroidFilesLauncher

    @Before
    fun setUp() {
        recordedIntents.clear()
        recordedErrors.clear()
        routeDirToTestExplorer = true

        val baseContext = ApplicationProvider.getApplicationContext<Context>()
        val testContext = InstrumentationRegistry.getInstrumentation().context
        val recordingContext = object : ContextWrapper(baseContext) {
            override fun startActivity(intent: Intent) {
                val launchIntent = if (
                    routeDirToTestExplorer &&
                    intent.type == DocumentsContract.Document.MIME_TYPE_DIR
                ) {
                    Intent(intent).apply {
                        component = ComponentName(testContext, TestFileExplorerActivity::class.java)
                    }
                } else {
                    intent
                }
                recordedIntents += launchIntent
                try {
                    super.startActivity(launchIntent)
                } catch (t: Throwable) {
                    recordedErrors += t
                    throw t
                }
            }
        }
        launcher = AndroidFilesLauncher(recordingContext)
    }

    @Test
    fun open_local_file_launches_precise_folder_without_security_exception() {
        launcher.open(
            UserFile(
                name = "episode.mp4",
                path = "content://media/external/video/media/1",
                realPath = "Movies/episode.mp4",
                source = FileSource.LOCAL,
            )
        )

        assertTrue("Expected no errors, got: $recordedErrors", recordedErrors.isEmpty())
        assertEquals(1, recordedIntents.size)
        val intent = recordedIntents.single()
        assertEquals(
            "content://com.android.externalstorage.documents/document/primary%3AMovies",
            intent.data.toString(),
        )
        assertEquals(DocumentsContract.Document.MIME_TYPE_DIR, intent.type)
    }

    @Test
    fun open_local_file_in_subfolder_launches_precise_subfolder_without_security_exception() {
        launcher.open(
            UserFile(
                name = "Frieren.S01E01.mp4",
                path = "content://media/external/video/media/2",
                realPath = "Movies/Flux/Frieren.S01E01.mp4",
                source = FileSource.LOCAL,
            )
        )

        assertTrue("Expected no errors, got: $recordedErrors", recordedErrors.isEmpty())
        assertEquals(1, recordedIntents.size)
        val intent = recordedIntents.single()
        assertEquals(
            "content://com.android.externalstorage.documents/document/primary%3AMovies%2FFlux",
            intent.data.toString(),
        )
        assertEquals(DocumentsContract.Document.MIME_TYPE_DIR, intent.type)
    }

    @Test
    fun openDownloadedImages_launches_downloaded_images_folder_without_security_exception() {
        launcher.openDownloadedImages()

        assertTrue("Expected no errors, got: $recordedErrors", recordedErrors.isEmpty())
        assertEquals(1, recordedIntents.size)
        val intent = recordedIntents.single()
        assertEquals(
            "content://com.android.externalstorage.documents/document/primary%3APictures%2FFlux",
            intent.data.toString(),
        )
        assertEquals(DocumentsContract.Document.MIME_TYPE_DIR, intent.type)
    }

    @Test
    fun open_saf_file_without_persisted_grant_does_not_throw_security_exception() {
        launcher.open(
            UserFile(
                name = "Big Buck Bunny.mp4",
                path = "content://com.android.externalstorage.documents/tree/primary%3AMovies/document/primary%3AMovies%2FFlux%2FBig%20Buck%20Bunny.mp4",
                realPath = "Flux/Big Buck Bunny.mp4",
                source = FileSource.SAF,
            )
        )

        assertTrue("Expected no errors, got: $recordedErrors", recordedErrors.isEmpty())
        assertEquals(1, recordedIntents.size)
    }

    @Test
    fun open_local_file_with_system_documents_ui_launches_without_security_exception() {
        routeDirToTestExplorer = false

        launcher.open(
            UserFile(
                name = "Frieren.S01E01.mp4",
                path = "content://media/external/video/media/2",
                realPath = "Movies/Flux/Frieren.S01E01.mp4",
                source = FileSource.LOCAL,
            )
        )

        assertTrue("Expected no errors, got: $recordedErrors", recordedErrors.isEmpty())
        assertEquals(1, recordedIntents.size)
        val intent = recordedIntents.single()
        assertEquals(
            "content://com.android.externalstorage.documents/document/primary%3AMovies%2FFlux",
            intent.data.toString(),
        )
        assertEquals(DocumentsContract.Document.MIME_TYPE_DIR, intent.type)
    }

}
