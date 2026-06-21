package com.mskd.flux.data.repository.token

import kotlinx.coroutines.flow.Flow

interface TokenRepository {

    val flow: Flow<String>

    suspend fun getToken(): String
    suspend fun saveToken(token: String)

    suspend fun clearToken()

    suspend fun dontRequestToken()

    val tokenRequested: Boolean

}