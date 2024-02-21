package com.kaem.flux.model.tmdb

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class TMDBCrew(
    val job: String,
    val department: String,
    @SerializedName("credit_id")
    val creditId: String,
    val adult: Boolean,
    val gender: Int,
    val id: Int,
    val name: String,
    @SerializedName("original_name")
    val originalName: String,
    val popularity: Float
) : Parcelable