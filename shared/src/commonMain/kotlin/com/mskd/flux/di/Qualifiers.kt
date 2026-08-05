package com.mskd.flux.di

import org.koin.core.qualifier.named

object Qualifiers {

    val CATALOG_DATASTORE = named("CATALOG_DATASTORE")
    val CUSTOMIZATION_DATASTORE = named("CUSTOMIZATION_DATASTORE")
    val SETTINGS_DATASTORE = named("SETTINGS_DATASTORE")
    val SNACKBAR_DATASTORE = named("SNACKBAR_DATASTORE")
    val TOKEN_DATASTORE = named("TOKEN_DATASTORE")
    val USER_DATASTORE = named("USER_DATASTORE")
    val DEFAULT_DISPATCHER = named("DEFAULT_DISPATCHER")
    val APPLICATION_SCOPE = named("APPLICATION_SCOPE")

}