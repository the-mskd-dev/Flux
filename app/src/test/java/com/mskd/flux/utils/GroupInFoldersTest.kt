package com.mskd.flux.utils

import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.utils.extensions.groupInFolders
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GroupInFoldersTest : FunSpec({

    test("files sharing a TMDB ID are grouped in the same folder even if their titles differ") {

        val files = listOf(
            UserFile(name = "GoT {tmdb-1399} S01E01.mkv", path = "path/1"),
            UserFile(name = "Game of Thrones {tmdb-1399} S01E02.mkv", path = "path/2"),
        )

        val folders = files.groupInFolders()

        folders.size shouldBe 1
        folders.first().tmdbId shouldBe 1399L
        folders.first().files shouldBe files
    }

    test("files with different TMDB IDs are not grouped together") {

        val files = listOf(
            UserFile(name = "Dune {tmdb-438631}.mkv", path = "path/1"),
            UserFile(name = "Dune {tmdb-841}.mkv", path = "path/2"),
        )

        val folders = files.groupInFolders()

        folders.map { it.tmdbId }.toSet() shouldBe setOf(438631L, 841L)
    }

    test("files without TMDB ID are still grouped by title and year") {

        val files = listOf(
            UserFile(name = "Naruto S01E01.mkv", path = "path/1"),
            UserFile(name = "Naruto S01E02.mkv", path = "path/2"),
            UserFile(name = "Inception (2010).mkv", path = "path/3"),
        )

        val folders = files.groupInFolders()

        folders.size shouldBe 2
        folders.all { it.tmdbId == null } shouldBe true
        folders.find { it.title == "naruto" }?.files?.size shouldBe 2
        folders.find { it.title == "inception" }?.year shouldBe 2010
    }

})
