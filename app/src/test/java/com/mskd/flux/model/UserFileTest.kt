package com.mskd.flux.model

import android.net.Uri
import com.mskd.flux.core.model.artwork.ContentType
import com.mskd.flux.core.model.catalog.CatalogFolder
import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.utils.extensions.groupInFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.datatest.withData
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.unmockkStatic
import kotlin.time.Instant

class UserFileTest : FunSpec ({

    context("movies - name properties from name") {
        withData(
            nameFn = { it.file.name },
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Inception.mp4",
                    path = "",
                ),
                expectedTitle = "inception",
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Spider-man(2002).mp4",
                    path = "",
                ),
                expectedTitle = "spider man",
                expectedYear = 2002
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Captain-America-The-Winter-Soldier-(2014).mp4",
                    path = "",
                ),
                expectedTitle = "captain america the winter soldier",
                expectedYear = 2014
            )
        ) { testCase ->

            val nameProperties = testCase.file.nameProperties
            nameProperties.title.shouldBe(testCase.expectedTitle, "Title mismatch")
            nameProperties.year.shouldBe(testCase.expectedYear, "Year mismatch")
            nameProperties.season.shouldBe(testCase.expectedSeason, "Season mismatch")
            nameProperties.episode.shouldBe(testCase.expectedEpisode, "Episode mismatch")

        }
    }

    context("movies - name properties from path") {
        withData(
            nameFn = { it.file.name },
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "INCEPTION.MULTI.VF2.1080P.WEB.X264-FW.MKV",
                    path = "",
                    realPath = "Flux/Inception/INCEPTION.MULTI.VF2.1080P.WEB.X264-FW.MKV"
                ),
                expectedTitle = "inception",
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "SPIDER-MAN.2002.MULTI.VF2.1080P.WEB.X264-FW.MKV",
                    path = "",
                    realPath = "Flux/Spider-man (2002)/SPIDER-MAN.2002.MULTI.VF2.1080P.WEB.X264-FW.MKV"
                ),
                expectedTitle = "spider man",
                expectedYear = 2002
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Captain.America.The.Winter.Soldier.2014.MULTI.VF2.1080P.WEB.X264-FW.MKV",
                    path = "",
                    realPath = "Flux/Captain America The Winter Soldier (2014)/Captain.America.The.Winter.Soldier.2014.MULTI.VF2.1080P.WEB.X264-FW.MKV"
                ),
                expectedTitle = "captain america the winter soldier",
                expectedYear = 2014
            ),
        ) { testCase ->

            val nameProperties = testCase.file.nameProperties
            nameProperties.title.shouldBe(testCase.expectedTitle, "Title mismatch")
            nameProperties.year.shouldBe(testCase.expectedYear, "Year mismatch")
            nameProperties.season.shouldBe(testCase.expectedSeason, "Season mismatch")
            nameProperties.episode.shouldBe(testCase.expectedEpisode, "Episode mismatch")

        }
    }

    context("shows - name properties from name") {
        withData(
            nameFn = { it.file.name },
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Naruto S01E01.mp4",
                    path = "",
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 1
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Naruto S01E133.mp4",
                    path = "",
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 133
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Naruto S01E1033.mp4",
                    path = "",
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 1033
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Naruto_s02e09.mp4",
                    path = "",
                ),
                expectedTitle = "naruto",
                expectedSeason = 2,
                expectedEpisode = 9
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective Conan_s01.e02.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective Conan_s1e2.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective-Conan_s1e2.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective Conan_s01e02.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective-Conan_s01e02.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective Conan_1x02.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective-Conan_1x02.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective Conan_se1.ep2.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective-Conan_se1.ep2.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective Conan-season1.episode2.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Detective-Conan-season1.episode2.mkv",
                    path = "",
                ),
                expectedTitle = "detective conan",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Hunter-X-Hunter (2011) season1.episode2.mkv",
                    path = "",
                ),
                expectedTitle = "hunter x hunter",
                expectedSeason = 1,
                expectedEpisode = 2,
                expectedYear = 2011
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "Hunter-X-Hunter (1999)_s01e02.mkv",
                    path = "",
                ),
                expectedTitle = "hunter x hunter",
                expectedSeason = 1,
                expectedEpisode = 2,
                expectedYear = 1999
            ),
        ) { testCase ->

            val nameProperties = testCase.file.nameProperties
            nameProperties.title.shouldBe(testCase.expectedTitle, "Title mismatch")
            nameProperties.year.shouldBe(testCase.expectedYear, "Year mismatch")
            nameProperties.season.shouldBe(testCase.expectedSeason, "Season mismatch")
            nameProperties.episode.shouldBe(testCase.expectedEpisode, "Episode mismatch")

        }
    }

    context("shows - name properties from path") {
        withData(
            nameFn = { it.file.realPath },
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "01.mp4",
                    path = "",
                    realPath = "Flux/Naruto/1/01.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 1
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "S01E02.mp4",
                    path = "",
                    realPath = "Flux/Naruto/S01E02.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 2
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "1.mp4",
                    path = "",
                    realPath = "Flux/Naruto/2/1.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 2,
                expectedEpisode = 1
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "1.mp4",
                    path = "",
                    realPath = "Flux/Naruto/season 1/3.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 3
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "1.mp4",
                    path = "",
                    realPath = "Flux/Naruto/Season3/1.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 3,
                expectedEpisode = 1
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "04.mp4",
                    path = "",
                    realPath = "Flux/Naruto/Season 01/04.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 1,
                expectedEpisode = 4
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "s05.e01.mp4",
                    path = "",
                    realPath = "Flux/Naruto/s05.e01.mp4"
                ),
                expectedTitle = "naruto",
                expectedSeason = 5,
                expectedEpisode = 1
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "NARUTO.S04E1045.MULTi.1080p.BluRay.x265-FERVEX.mkv",
                    path = "",
                    realPath = "Naruto/Season 4/NARUTO.S04E1045.MULTi.1080p.BluRay.x265-FERVEX.mkv"
                ),
                expectedTitle = "naruto",
                expectedSeason = 4,
                expectedEpisode = 1045
            ),
            UserFileTestCases.FileProperties(
                file = UserFile(
                    name = "10.mkv",
                    path = "",
                    realPath = "Naruto/S5/10.mkv"
                ),
                expectedTitle = "naruto",
                expectedSeason = 5,
                expectedEpisode = 10
            ),
        ) { testCase ->

            val nameProperties = testCase.file.nameProperties
            nameProperties.title.shouldBe(testCase.expectedTitle, "Title mismatch")
            nameProperties.year.shouldBe(testCase.expectedYear, "Year mismatch")
            nameProperties.season.shouldBe(testCase.expectedSeason, "Season mismatch")
            nameProperties.episode.shouldBe(testCase.expectedEpisode, "Episode mismatch")

        }
    }

    test("group files in folders") {

        val files = FilesMockups.mediaStoreFiles + FilesMockups.safFiles

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

        val userFile = UserFile(
            name = "Naruto S01E01.mp4",
            addedDateTime = 1621814400000L, // 2021-05-24
            path = "file:///path/to/Naruto S01E01.mp4",
            source = FileSource.LOCAL
        )

        userFile.isEpisode shouldBe true
        userFile.season shouldBe 1
        userFile.episode shouldBe 1
        userFile.addedDate shouldBe Instant.fromEpochMilliseconds(1621814400000L)

        unmockkStatic(Uri::class)
    }

    test("CatalogFolder type resolver") {

        val episodeFile1 = UserFile(name = "Naruto S01E01.mp4", addedDateTime = 0L, path = "path")
        val episodeFile2 = UserFile("Naruto S01E02.mp4", 0L, "path")
        val movieFile1 = UserFile("Inception.mp4", 0L, "path")
        val movieFile2 = UserFile("Spider-man.mp4", 0L, "path")

        // Show folder
        val showFolder = CatalogFolder(title = "naruto", files = listOf(episodeFile1, episodeFile2))
        showFolder.type shouldBe ContentType.SHOW

        // Movie folder
        val movieFolder = CatalogFolder(title = "inception", files = listOf(movieFile1))
        movieFolder.type shouldBe ContentType.MOVIE

        // Mixed/multiple movies folder -> should be null type
        val mixedFolder = CatalogFolder(title = "mixed", files = listOf(movieFile1, movieFile2))
        mixedFolder.type shouldBe null

    }

    test("quick test") {

        val userFile = UserFile(
            name = "Spider.man.brand.new.day.mp4",
            path = "",
            realPath = "La légende de Korra/Season 1/Avatar.La.legende.de.Korra.S01E01.MULTi.1080p.BluRay.x265-FERVEX.mkv"
        )

        val properties = userFile.nameProperties

        println(
            """
            === NameProperties Inspection ===
            Title   : ${properties.title}
            Year    : ${properties.year}
            Season  : ${properties.season}
            Episode : ${properties.episode}
            =================================
            """.trimIndent()
        )


    }

})