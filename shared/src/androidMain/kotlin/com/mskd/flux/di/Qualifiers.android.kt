package com.mskd.flux.di

import org.koin.core.qualifier.named

object QualifiersAndroid {

    val PLAYER_SERVICE_SCOPE = named("PLAYER_SERVICE_SCOPE")

    val MEDIASTORE_SOURCES = named("MEDIASTORE_SOURCES")
    val SAF_SOURCES = named("SAF_SOURCES")

}