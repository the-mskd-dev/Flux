package com.mskd.flux.mockups

import com.mskd.flux.model.domain.files.FileSource
import com.mskd.flux.core.domain.model.files.UserFile
import com.mskd.flux.features.sources.domain.model.UserFolder

object FilesMockups {

    val localFiles = listOf(
        UserFile(
            name = "naruto_S01E01.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "naruto _S02E01.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = " naruto_S02E02.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "Naruto_S04E10.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "your name.mp4",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "spider-man(2002).mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "Hunter x Hunter (2011) season1.episode2.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        ),
        UserFile(
            name = "Hunter x Hunter (1999) season1.episode2.mkv",
            addedDateTime = 0L,
            path = "localPath",
            source = FileSource.LOCAL
        )
    )

    val userFolders = listOf(
        UserFolder(
            path = "path/to/folder",
            source = FileSource.LOCAL,
            status = UserFolder.Status.AVAILABLE
        ),
        UserFolder(
            path = "path/to/folder2",
            source = FileSource.LOCAL,
            status = UserFolder.Status.AVAILABLE
        ),
        UserFolder(
            path = "path/to/missingFolder",
            source = FileSource.LOCAL,
            status = UserFolder.Status.MISSING
        )
    )
}