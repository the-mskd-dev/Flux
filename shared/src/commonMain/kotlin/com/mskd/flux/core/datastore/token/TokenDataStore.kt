package com.mskd.flux.core.datastore.token

import kotlinx.coroutines.flow.Flow

interface TokenDataStore {

    val flow: Flow<String>

    suspend fun getToken(): String
    suspend fun saveToken(token: String)

    suspend fun clearToken()

    suspend fun dontRequestToken()

    val tokenRequested: Boolean

}