package com.mskd.flux.features.sources.domain.model

import androidx.core.net.toUri

val UserFolder.name: String get() = this.path.toUri().lastPathSegment ?: this.path