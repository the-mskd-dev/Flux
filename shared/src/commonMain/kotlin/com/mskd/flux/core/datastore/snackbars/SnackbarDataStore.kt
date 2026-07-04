package com.mskd.flux.core.datastore.snackbars

import kotlinx.coroutines.flow.Flow

interface SnackbarDataStore {

    fun canShow(snackbarId: String) : Flow<Boolean>

    fun getCount(snackbarId: String) : Flow<Int>

    suspend fun incrementCount(snackbarId: String)

}