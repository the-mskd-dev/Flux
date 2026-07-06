package com.mskd.flux.utils

import io.github.aakira.napier.Napier

object Trace {

    fun info(tag: String, message: String) {
        Napier.i(tag = tag, message = message)
    }

    fun debug(message: String, tag: String = "TEST") {
        Napier.d(tag = tag, message = message)
    }

    fun error(tag: String, message: String, throwable: Throwable? = null) {
        Napier.e(tag = tag, message = message, throwable = throwable)
    }

}