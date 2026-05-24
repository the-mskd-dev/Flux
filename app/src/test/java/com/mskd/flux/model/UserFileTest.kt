package com.mskd.flux.model

import android.net.Uri
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.model.artwork.ContentType
import com.mskd.flux.utils.extensions.groupInFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic

class UserFileTest : FunSpec ({

    context("test file name properties parsing for movies") {
        withData(
            nameFn = { it.fileName },
            UserFileTestCases.FileProperties(
                fileName = "Inception.mp4",
                expectedTitle = "inception",
            ),
            UserFileTestCases.FileProperties(
                fileName = "Spider-man(2001).mp4",
                expectedTitle = "spider man",
                expectedYear = 2001
            ),
            UserFileTestCases.FileProperties(
                fileName = "Spider-man (2001).mp4",
                expectedTitle = "spider man",
                expectedYear = 2001
            ),
            UserFileTestCases.FileProperties(
                fileName = "Captain-America-The-Winter-Soldier-(2014).mp4",
                expectedTitle = "captain america the winter soldier",
                expectedYear = 2014
            )
        ) { testCase ->

            val mediaInfo = FileProperties.extractFileProperties(testCase.fileName)
            mediaInfo.title.shouldBe(testCase.expectedTitle, "Title mismatch")
            mediaInfo.year.shouldBe(testCase.expectedYear, "Year mismatch")
            mediaInfo.season.shouldBe(testCase.expectedSeason, "Season mismatch")
            mediaInfo.episode.shouldBe(testCase.expectedEpisode, "Episode mismatch")

        }
    }

    context("test file name properties parsing for shows") {
        withData(
            nameFn = { it.fileName },
            UserFileTestCases.FileProperties(
                fileName = "Naruto S01E01.mp4",
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 1
            ),
            UserFileTestCases.FileProperties(
                fileName = "Naruto_s02e09.mp4",
                expectedTitle = "naruto",
                expectedSeason = 2,
                expectedEpisode = 9
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective Conan_s01.e02.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective Conan_s1e2.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective-Conan_s1e2.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective Conan_s01e02.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective-Conan_s01e02.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective Conan_1x02.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective-Conan_1x02.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective Conan_se1.ep2.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective-Conan_se1.ep2.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective Conan-season1.episode2.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Detective-Conan-season1.episode2.mkv",
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                fileName = "Hunter-X-Hunter (2011) season1.episode2.mkv",
                expectedTitle = "hunter x hunter",
                expectedSeason = 1,
                expectedEpisode = 2,
                expectedYear = 2011
            ),
            UserFileTestCases.FileProperties(
                fileName = "Hunter-X-Hunter (1999)_s01e02.mkv",
                expectedTitle = "hunter x hunter",
                expectedSeason = 1,
                expectedEpisode = 2,
                expectedYear = 1999
            ),
        ) { testCase ->

            val mediaInfo = FileProperties.extractFileProperties(testCase.fileName)
            mediaInfo.title.shouldBe(testCase.expectedTitle, "Title mismatch")
            mediaInfo.year.shouldBe(testCase.expectedYear, "Year mismatch")
            mediaInfo.season.shouldBe(testCase.expectedSeason, "Season mismatch")
            mediaInfo.episode.shouldBe(testCase.expectedEpisode, "Episode mismatch")

        }
    }

    test("group files in folders") {

        val files = FilesMockups.localFiles

        val folders = files.groupInFolders()
        val narutoFolder = folders.find { it.title == "naruto" }
        val oldHxHFolder = folders.find { it.title == "hunter x hunter" && it.year == 1999 }
        val newHxHFolder = folders.find { it.title == "hunter x hunter" && it.year == 2011 }

        folders.size shouldBe 5
        oldHxHFolder shouldNotBe null
        newHxHFolder shouldNotBe null
        narutoFolder?.files?.size shouldBe 4
    }

    test("UserFile properties") {
        mockkStatic(Uri::class)
        val mockUri = mockk<Uri>(relaxed = true)
        every { Uri.parse("file:///path/to/Naruto S01E01.mp4") } returns mockUri

        val userFile = UserFile(
            name = "Naruto S01E01.mp4",
            addedDateTime = 1621814400000L, // 2021-05-24
            path = "file:///path/to/Naruto S01E01.mp4",
            source = FileSource.LOCAL
        )

        userFile.isEpisode shouldBe true
        userFile.season shouldBe 1
        userFile.episode shouldBe 1
        userFile.addedDate shouldBe java.util.Date(1621814400000L)
        userFile.uri shouldBe mockUri

        unmockkStatic(Uri::class)
    }

    test("UserFolder type resolver") {
        mockkStatic(Uri::class)
        every { Uri.parse(any()) } returns mockk(relaxed = true)

        val episodeFile1 = UserFile("Naruto S01E01.mp4", 0L, "path", FileSource.LOCAL)
        val episodeFile2 = UserFile("Naruto S01E02.mp4", 0L, "path", FileSource.LOCAL)
        val movieFile1 = UserFile("Inception.mp4", 0L, "path", FileSource.LOCAL)
        val movieFile2 = UserFile("Spider-man.mp4", 0L, "path", FileSource.LOCAL)

        // Show folder
        val showFolder = UserFolder(title = "naruto", files = listOf(episodeFile1, episodeFile2))
        showFolder.type shouldBe ContentType.SHOW

        // Movie folder
        val movieFolder = UserFolder(title = "inception", files = listOf(movieFile1))
        movieFolder.type shouldBe ContentType.MOVIE

        // Mixed/multiple movies folder -> should be null type
        val mixedFolder = UserFolder(title = "mixed", files = listOf(movieFile1, movieFile2))
        mixedFolder.type shouldBe null

        unmockkStatic(Uri::class)
    }

})