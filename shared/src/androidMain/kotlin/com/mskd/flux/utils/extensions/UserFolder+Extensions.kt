package com.mskd.flux.utils.extensions

import androidx.core.net.toUri
import com.mskd.flux.model.domain.files.UserFolder

val UserFolder.name: String get() = this.path.toUri().lastPathSegment ?: this.path