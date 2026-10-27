package com.mskd.flux.core.network.tmdb.data

import io.ktor.client.engine.HttpClientEngine

expect fun tmdbEngine(): HttpClientEngine
