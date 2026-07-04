package com.mskd.flux.core.domain.model.core

data class AppInfo(
    val versionCode: Int,
    val versionName: String,
    val isDebug: Boolean = false,
    val debugToken: String = ""
)