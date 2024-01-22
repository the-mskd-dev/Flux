package com.kaem.flux.model

import android.net.Uri

data class FluxMedia(
    val name: String,
    val season: Int?,
    val episode: Float?,
    val source: FluxSource
) {

    val isMovie: Boolean = season == null && episode == null

    companion object {

        fun parse(name: String) : Triple<String, Int?, Float?>? {

            val splitFileName = name.split("_")

            return when (splitFileName.size) {

                1 -> Triple(splitFileName[0], null, null)

                2 -> {

                    val seasonAndEpisode = splitFileName[1]
                        .lowercase()
                        .removePrefix("s")
                        .split("e")

                    val season = seasonAndEpisode.getOrNull(0)?.toIntOrNull()
                    val episode = seasonAndEpisode.getOrNull(1)?.toFloatOrNull()

                    Triple(
                        splitFileName[0],
                        season,
                        episode
                    )

                }

                else -> null

            }

        }

    }
}