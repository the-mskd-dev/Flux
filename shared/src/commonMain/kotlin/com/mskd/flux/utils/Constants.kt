package com.mskd.flux.utils

import com.mskd.flux.core.model.language.toTmdbFormat
import com.mskd.flux.system.systemLanguage

object Constants {

    object TMDB {
        const val IMAGE_LARGE = "https://image.tmdb.org/t/p/original"
        const val IMAGE = "https://image.tmdb.org/t/p/w300"

        const val LOG_IN = "https://www.themoviedb.org/login?to=read_me&redirect=%2Fdocs%2Fgetting-started"

        const val WEBSITE = "https://www.themoviedb.org/"
        const val SIGN_UP = "https://www.themoviedb.org/signup"
        const val GET_API_TOKEN = "https://www.themoviedb.org/settings/api"

    }

    object CONTACT {
        const val MAIL = "the.masked.dev@proton.me"
        const val GITHUB = "https://github.com/the-mskd-dev/Flux"

        const val X = "https://x.com/themskddev"
        const val RELEASES = "https://github.com/the-mskd-dev/Flux/releases"
        const val ISSUES = "https://github.com/the-mskd-dev/Flux/issues"
        const val BUY_COFFEE = "https://buymeacoffee.com/the.masked.dev"
        const val SPONSOR = "https://github.com/sponsors/the-mskd-dev/"
    }

    object PLAYER {
        const val PROGRESS_THRESHOLD = 0.92
    }

}