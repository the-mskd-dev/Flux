package com.mskd.flux.data.repository.snackbars

import kotlinx.coroutines.flow.Flow

interface SnackbarRepository {

    fun canShow(snackbarId: String) : Flow<Boolean>

    fun getCount(snackbarId: String) : Flow<Int>

    suspend fun incrementCount(snackbarId: String)

}