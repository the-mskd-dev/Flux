package com.kaem.flux.configs

import io.kotest.core.spec.Extendable

fun Extendable.fluxExtensions() {
    extension(DispatcherConfig())
    extension(LogConfig())
}