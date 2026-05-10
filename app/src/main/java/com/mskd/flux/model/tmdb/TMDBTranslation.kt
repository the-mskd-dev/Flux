package com.mskd.flux.model.tmdb

import com.google.gson.annotations.SerializedName

data class TMDBTranslation(
    val name: String,
    @SerializedName("english_name")
    val englishName: String,
    val data: Data
) {

    data class Data(
        val name: String,
        val overview: String
    )

}
