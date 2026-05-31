package com.mskd.flux.utils.interceptors

import coil3.intercept.Interceptor
import coil3.request.CachePolicy
import coil3.request.ImageResult
import com.mskd.flux.data.repository.connectivity.ConnectivityRepository
import javax.inject.Inject

class NetworkImageInterceptor @Inject constructor(
    private val connectivityRepository: ConnectivityRepository
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        if (connectivityRepository.currentlyOnline()) {
            return chain.proceed()
        }

        val newChain = chain.withRequest(
            chain.request.newBuilder()
                .networkCachePolicy(CachePolicy.DISABLED)
                .build()
        )

        return newChain.proceed()
    }

}