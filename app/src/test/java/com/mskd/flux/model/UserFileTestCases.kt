package com.mskd.flux.model

import com.mskd.flux.core.model.files.UserFile

object UserFileTestCases {

    data class FileProperties(
        val file: UserFile,
        val expectedTitle: String,
        val expectedYear: Int? = null,
        val expectedSeason: Int? = null,
        val expectedEpisode: Int? = null
    )

}