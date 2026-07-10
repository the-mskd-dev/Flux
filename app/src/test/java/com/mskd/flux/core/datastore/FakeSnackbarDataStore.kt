package com.mskd.flux.core.datastore

import com.mskd.flux.core.datastore.domain.SnackbarDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSnackbarDataStore : SnackbarDataStore {

    override fun canShow(snackbarId: String): Flow<Boolean> = MutableStateFlow(true)

    override fun getCount(snackbarId: String): Flow<Int> = MutableStateFlow(0)

    override suspend fun incrementCount(snackbarId: String) {}

}