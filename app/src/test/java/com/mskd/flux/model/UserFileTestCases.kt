package com.mskd.flux.model

object UserFileTestCases {

    data class FileProperties(
        val fileName: String,
        val expectedTitle: String,
        val expectedYear: Int? = null,
        val expectedSeason: Int? = null,
        val expectedEpisode: Int? = null
    )

}