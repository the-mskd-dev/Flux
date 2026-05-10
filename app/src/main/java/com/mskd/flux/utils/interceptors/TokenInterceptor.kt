package com.mskd.flux.utils.interceptors

import com.mskd.flux.data.tmdb.token.TokenRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class TokenInterceptor @Inject constructor(
    private val tokenRepository: TokenRepository
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()
        val token = runBlocking { tokenRepository.getToken() }

        val newRequest = request.newBuilder()
            .addHeader("accept", "application/json")
            .apply {
                if (token.isNotEmpty()) {
                    addHeader("Authorization", "Bearer ${token.trim()}")
                }
            }
            .build()

        return chain.proceed(newRequest)

    }

}