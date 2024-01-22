package com.kaem.flux.model

data class FileName(
    val name: String,
    val season: Int = 0,
    val episode: Float = 0f
) {


    companion object {

        fun parse(name: String) : FileName? {

            val splitFileName = name.split("_")

            return when (splitFileName.size) {

                1 -> FileName(name = splitFileName[0])

                2 -> {

                    val seasonAndEpisode = splitFileName[1]
                        .lowercase()
                        .removePrefix("s")
                        .split("e")

                    val season = seasonAndEpisode.getOrNull(0)?.toIntOrNull() ?: 0
                    val episode = seasonAndEpisode.getOrNull(1)?.toFloatOrNull() ?: 0f

                    FileName(
                        name = splitFileName[0],
                        season = season,
                        episode = episode
                    )

                }

                else -> null

            }

        }

    }

}
