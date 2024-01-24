package com.kaem.flux.utils

import java.util.Locale

object Constants {

    val LANGUAGE get() = "${Locale.getDefault().language}-${Locale.getDefault().country}"

}