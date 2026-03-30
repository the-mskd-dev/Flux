package com.mskd.flux.model.tmdb

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

/**
 * Represents a crew member associated with a movie or TV show episode.
 *
 * @property job Job role of the crew member.
 * @property department Department to which the crew member belongs.
 * @property creditId Unique ID of the credit.
 * @property adult Indicates whether the crew member is associated with adult content.
 * @property gender Gender of the crew member (e.g., 1 for female, 2 for male).
 * @property id Unique identifier for the crew member.
 * @property name Name of the crew member.
 * @property originalName Original name of the crew member.
 * @property popularity Popularity score of the crew member.
 */
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