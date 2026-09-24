package com.mskd.flux.core.model.core

enum class Flavor {
    FOSS, PLAY_STORE;

    companion object {

        fun fromString(text: String): Flavor {
            return when (text) {
                "playstore" -> PLAY_STORE
                else -> FOSS
            }
        }
    }
}