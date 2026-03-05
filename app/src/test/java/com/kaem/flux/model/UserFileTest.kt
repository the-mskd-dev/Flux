package com.kaem.flux.model

import com.kaem.flux.mockups.FilesMockups
import com.kaem.flux.utils.extensions.groupInFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe


class UserFileTest : FunSpec ({

    fun asserFileProperties(
        filename: String,
        expectedTitle: String,
        expectedYear: Int?,
        expectedSeason: Int?,
        expectedEpisode: Int?
    ) {
        val mediaInfo = FileProperties.extractFileProperties(filename)
        expectedTitle.shouldBe(mediaInfo.title, "Title mismatch for file: $filename, found ${mediaInfo.title}")
        expectedYear.shouldBe(mediaInfo.year, "Year mismatch for file: $filename, found ${mediaInfo.year}")
        expectedSeason.shouldBe(mediaInfo.season, "Season mismatch for file: $filename, found ${mediaInfo.season}")
        expectedEpisode.shouldBe(mediaInfo.episode, "Episode mismatch for file: $filename, found ${mediaInfo.episode}")
    }


    test("test file name properties parsing") {
        // Movies
        asserFileProperties("Spider-man(2001).mp4", "spider man", 2001, null, null)
        asserFileProperties("Pulp Fiction (1994).mkv", "pulp fiction", 1994, null, null)
        asserFileProperties("Inception.avi", "inception", null, null, null)
        asserFileProperties("Captain-America-The-Winter-Soldier-(2014).mp4", "captain america the winter soldier", 2014, null, null)

        // Episodes
        asserFileProperties("Naruto_s02e09.mp4", "naruto", null, 2, 9)
        asserFileProperties("Detective Conan_s01.e02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_s01.e02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan_s1e2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_s1e2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan_s01e02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_s01e02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan_1x02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_1x02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan_se1.ep2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_se1.ep2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan-season1.episode2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan-season1.episode2.mkv", "detective conan", null, 1, 2)
    }

    test("group files in folders") {

        val files = FilesMockups.localFiles

        val folders = files.groupInFolders()
        val narutoFolder = folders.firstOrNull { it.title == "naruto" }

        folders.size shouldBe 3
        narutoFolder?.files?.size shouldBe 4
    }

})