package com.kaem.flux.model.flux

import com.kaem.flux.model.FileSource

data class FluxFile(
    val name: String,
    val source: FileSource
)