package com.mskd.flux.mockups

import com.mskd.flux.core.model.files.FileSource
import com.mskd.flux.core.model.files.UserFile
import com.mskd.flux.features.sources.domain.model.UserFolder

object FilesMockups {

    val mediaStoreFiles = listOf(
        UserFile(
            name = "naruto_S01E01.mkv",
            addedDateTime = 0L,
            path = "mediaStore/naruto_S01E01.mkv",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "naruto _S02E01.mkv",
            addedDateTime = 0L,
            path = "mediaStore/naruto _S02E01.mkv",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = " naruto_S02E02.mkv",
            addedDateTime = 0L,
            path = "mediaStore/ naruto_S02E02.mkv",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "Naruto_S04E10.mkv",
            addedDateTime = 0L,
            path = "mediaStore/Naruto_S04E10.mkv",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "your name.mp4",
            addedDateTime = 0L,
            path = "mediaStore/your name.mp4",
            source = FileSource.LOCAL
        ),
    )

    val safFiles = listOf(
        UserFile(
            name = "spider-man(2002).mkv",
            addedDateTime = 0L,
            path = "saf/spider-man(2002).mkv",
            source = FileSource.SAF
        ),
        UserFile(
            name = "Hunter x Hunter (2011) season1.episode2.mkv",
            addedDateTime = 0L,
            path = "saf/Hunter x Hunter (2011) season1.episode2.mkv",
            source = FileSource.SAF
        ),
        UserFile(
            name = "Hunter x Hunter (1999) season1.episode2.mkv",
            addedDateTime = 0L,
            path = "saf/Hunter x Hunter (1999) season1.episode2.mkv",
            source = FileSource.SAF
        )
    )

    val subtitles = listOf(
        "naruto_S01E01",
        "spider-man(2002)"
    )

    val userFolders = listOf(
        UserFolder(
            path = "path/to/folder",
            source = FileSource.LOCAL,
            isAvailable = true
        ),
        UserFolder(
            path = "path/to/folder2",
            source = FileSource.LOCAL,
            isAvailable = true
        ),
        UserFolder(
            path = "path/to/missingFolder",
            source = FileSource.LOCAL,
            isAvailable = false
        )
    )

}