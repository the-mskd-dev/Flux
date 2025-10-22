package com.kaem.flux.utils

import java.util.Locale

object Constants {

    object Behaviour {
        const val TRANSITION_SPEED = 400
    }

    object Global {
        val LANGUAGE get() = "${Locale.getDefault().language}-${Locale.getDefault().country}"
    }

    object TMDB {
        const val IMAGE = "https://image.tmdb.org/t/p/original"
        const val IMAGE_SMALL = "https://image.tmdb.org/t/p/w500"
    }

}