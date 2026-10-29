package com.mskd.flux.core.network.tmdb.data

import io.ktor.client.plugins.HttpSend
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.plugin
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

class RequestLimiterConfig {
    var limit: Int = TMDB_MAX_REQUESTS
}

/** Bounds the requests in flight of a client: each attempt, retries included, takes one place until its response is received. */
val RequestLimiter = createClientPlugin("RequestLimiter", ::RequestLimiterConfig) {
    val semaphore = Semaphore(pluginConfig.limit)
    client.plugin(HttpSend).intercept { request -> semaphore.withPermit { execute(request) } }
}
