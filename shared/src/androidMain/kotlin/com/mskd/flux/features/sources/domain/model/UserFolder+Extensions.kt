package com.mskd.flux.features.sources.domain.model

import androidx.core.net.toUri
import java.net.URLDecoder
import java.nio.charset.StandardCharsets

val UserFolder.name: String get() {
    return try {

        val decodedName = URLDecoder.decode(this.path, StandardCharsets.UTF_8.name())
        decodedName
            .substringAfterLast("/")
            .substringAfter(":")

    } catch (_: Exception) {
        this.path.toUri().lastPathSegment ?: this.path
    }
}

val UserFolder.cleanPath : String  get() {
    return try {

        val decodedUri = URLDecoder.decode(this.path, StandardCharsets.UTF_8.name())

        val treePart = decodedUri.substringAfter("/tree/", "")

        if (treePart.isNotEmpty()) {
            treePart//.substringAfter(":")
        } else {
            this.path.substringAfterLast("/")
        }
    } catch (_: Exception) {
        this.path
    }
}