package com.mskd.flux.model.player

data class PlayerTrack(
    val id: String? = null,
    val label: String,
    val language: String? = null,
    val type: Type
) {

    enum class Type {
        AUDIO, SUBTITLES
    }

    companion object {
        val NO_SUBTITLES = PlayerTrack(
            id = null,
            label = "",
            language = null,
            type = Type.SUBTITLES
        )
    }

}