package com.mskd.flux.features.token.domain.datastore

import kotlinx.coroutines.flow.Flow

interface TokenDataStore {

    val flow: Flow<String>

    suspend fun tokenIsAvailable(): Boolean
    suspend fun getToken(): String
    suspend fun saveToken(token: String)

    suspend fun clearToken()

    suspend fun dontRequestToken()

    val tokenRequested: Boolean

}