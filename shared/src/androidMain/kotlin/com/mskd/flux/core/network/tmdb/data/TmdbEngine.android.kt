package com.mskd.flux.core.network.tmdb.data

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp

actual fun tmdbEngine(): HttpClientEngine = OkHttp.create()
