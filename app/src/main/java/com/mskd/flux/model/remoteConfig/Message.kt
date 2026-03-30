package com.mskd.flux.model.remoteConfig

data class Message(
    val id: Int,
    val versionCode: Int,
    val title: String,
    val subtitle: String,
    val message: String,
)