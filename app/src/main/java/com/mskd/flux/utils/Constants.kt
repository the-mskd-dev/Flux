package com.mskd.flux.utils

import java.util.Locale

object Constants {

    object Behaviour {
        const val TRANSITION_SPEED = 400
    }

    object Global {
        val LANGUAGE get() = "${Locale.getDefault().language}-${Locale.getDefault().country}"
    }

    object TMDB {
        const val IMAGE_LARGE = "https://image.tmdb.org/t/p/original"
        const val IMAGE = "https://image.tmdb.org/t/p/w500"

        const val LOG_IN = "https://www.themoviedb.org/login?to=read_me&redirect=%2Fdocs%2Fgetting-started"

        const val WEBSITE = "https://www.themoviedb.org/"
        const val SIGN_UP = "https://www.themoviedb.org/signup"
        const val GET_API_KEY = "https://www.themoviedb.org/settings/api"
    }

    object CONTACT {
        const val MAIL = "mailto:the.masked.dev@proton.me"
        const val GITHUB = "https://github.com/the-mskd-dev/Flux"
        const val RELEASES = "https://github.com/the-mskd-dev/Flux/releases"
        const val ISSUES = "https://github.com/the-mskd-dev/Flux/issues"
        const val BUY_COFFEE = "https://buymeacoffee.com/the.masked.dev"
    }

    object PLAYER {
        const val PROGRESS_THRESHOLD = 0.92
    }

}