package com.mskd.flux.features.gallery.data.datasource

import com.mskd.flux.features.gallery.domain.datasource.RemoteImageDataSource
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get

class KtorRemoteImageDataSource(
    private val client: HttpClient,
) : RemoteImageDataSource {

    override suspend fun fetch(url: String): ByteArray =
        client.get(url) { expectSuccess = true }.body()

}
