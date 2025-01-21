package com.kaem.flux.utils

import java.util.Locale

object Constants {

    object Behaviour {
        const val TRANSITION_SPEED = 400
    }

    object Global {
        val LANGUAGE get() = "${Locale.getDefault().language}-${Locale.getDefault().country}"
    }

    object Navigation {
        const val LIBRARY = "library"
        const val CATEGORY = "category"
        const val ARTWORK = "artwork"
        const val SEARCH = "search"
        const val SETTINGS = "settings"
        const val HOW_TO = "howTo"
    }

    object TMDB {
        const val IMAGE = "https://image.tmdb.org/t/p/original"
        const val IMAGE_SMALL = "https://image.tmdb.org/t/p/w500"
    }

}