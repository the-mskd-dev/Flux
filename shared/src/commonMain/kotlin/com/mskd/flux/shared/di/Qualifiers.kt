package com.mskd.flux.shared.di

import org.koin.core.qualifier.named

object Qualifiers {

    val CUSTOMIZATION_DATASTORE = named("CUSTOMIZATION_DATASTORE")
    val SETTINGS_DATASTORE = named("SETTINGS_DATASTORE")
    val SNACKBAR_DATASTORE = named("SNACKBAR_DATASTORE")
    val TOKEN_DATASTORE = named("TOKEN_DATASTORE")
    val USER_DATASTORE = named("USER_DATASTORE")

}