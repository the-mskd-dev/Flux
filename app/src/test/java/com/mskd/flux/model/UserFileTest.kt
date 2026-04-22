package com.mskd.flux.model

import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.utils.extensions.groupInFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe


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
        val narutoFolder = folders.firstOrNull { it.title == "naruto" }

        folders.size shouldBe 3
        narutoFolder?.files?.size shouldBe 4
    }

})