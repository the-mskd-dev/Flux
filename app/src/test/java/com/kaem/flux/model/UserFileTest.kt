package com.kaem.flux.model

import com.kaem.flux.mockups.FilesMockups
import com.kaem.flux.utils.extensions.groupInFolders
import org.junit.Test


class UserFileTest {


    @Test
    fun `test file name properties parsing`() {
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
        asserFileProperties("Detective Conan_1x02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_1x02.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan_se1.ep2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan_se1.ep2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective Conan-season1.episode2.mkv", "detective conan", null, 1, 2)
        asserFileProperties("Detective-Conan-season1.episode2.mkv", "detective conan", null, 1, 2)
    }

    @Test
    fun group_files_in_folders() {

        val files = FilesMockups.localFiles

        val folders = files.groupInFolders()

        assert(folders.size == 3)

        val narutoFolder = folders.firstOrNull { it.title == "naruto" }

        assert(narutoFolder?.files?.size == 4)
    }

    private fun asserFileProperties(
        filename: String,
        expectedTitle: String,
        expectedYear: Int?,
        expectedSeason: Int?,
        expectedEpisode: Int?
    ) {
        val mediaInfo = FileProperties.extractFileProperties(filename)
        assert(expectedTitle == mediaInfo.title) { "Title mismatch for file: $filename, found ${mediaInfo.title}" }
        assert(expectedYear == mediaInfo.year) { "Year mismatch for file: $filename, found ${mediaInfo.year}" }
        assert(expectedSeason == mediaInfo.season) { "Season mismatch for file: $filename, found ${mediaInfo.season}" }
        assert(expectedEpisode == mediaInfo.episode) { "Episode mismatch for file: $filename, found ${mediaInfo.episode}" }
    }

}