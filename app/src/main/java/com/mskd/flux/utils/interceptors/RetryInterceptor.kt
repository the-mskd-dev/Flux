package com.mskd.flux.utils.interceptors

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class RetryInterceptor @Inject constructor() : Interceptor {

    private val maxRetries: Int = 3

    override fun intercept(chain: Interceptor.Chain): Response {
        var retryCount = 0
        var response = chain.proceed(chain.request())

        while (response.code == 429 && retryCount < maxRetries) {
            response.close()
            retryCount++
            Thread.sleep(1000L * retryCount) // backoff progressif
            response = chain.proceed(chain.request())
        }

        return response
    }

}