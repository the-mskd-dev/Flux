package com.mskd.flux.features.catalog.sync

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpRequestData
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger

/**
 * TMDB simulated at the HTTP level: each request is answered with the JSON fixture found at
 * `tmdb/<path>.json` in the test resources, and recorded to count the calls per endpoint.
 *
 * A request with a `language` parameter is first looked up at `tmdb/<path>.<language code>.json` (`.en.json`),
 * so that the same endpoint can answer differently per language.
 *
 * A search is answered with `tmdb/search/<movie|tv>/<query slug>.json`, or with no result.
 * Any other request without fixture is answered with a TMDB "not found" error.
 */
class FakeTmdb {

    private companion object {
        const val BASE_PATH = "/3/"
        const val EMPTY_SEARCH = """{"page":1,"results":[],"total_pages":0,"total_results":0}"""
        const val NOT_FOUND = """{"success":false,"status_code":34,"status_message":"The resource you requested could not be found."}"""
        val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
    }

    private val calls = CopyOnWriteArrayList<String>()

    @Volatile
    private var gate: CompletableDeferred<Unit>? = null

    @Volatile
    private var endpointGate: Pair<Regex, CompletableDeferred<Unit>>? = null

    @Volatile
    private var networkDown = false

    private val failingEndpoints = CopyOnWriteArrayList<Pair<Regex, String?>>()

    private val remainingTooManyRequests = AtomicInteger()

    private val inFlight = AtomicInteger()

    /** Most requests that were awaiting an answer at the same time. */
    @Volatile
    var maxInFlight = 0
        private set

    /** Answers the next [times] requests with a 429 response. */
    fun tooManyRequests(times: Int) = remainingTooManyRequests.set(times)

    /** Time taken to answer each request, to let the requests overlap. */
    @Volatile
    var latencyMillis = 0L

    val engine = MockEngine { request ->

        val path = request.url.encodedPath.removePrefix(BASE_PATH)
        calls += path
        val current = inFlight.incrementAndGet()
        synchronized(this@FakeTmdb) { if (current > maxInFlight) maxInFlight = current }
        try {
            delay(latencyMillis)
            respondTo(request, path)
        } finally {
            inFlight.decrementAndGet()
        }
    }

    private suspend fun MockRequestHandleScope.respondTo(request: HttpRequestData, path: String): HttpResponseData {
        gate?.await()
        endpointGate?.let { (endpoint, gate) -> if (endpoint.matches(path)) gate.await() }

        if (remainingTooManyRequests.getAndUpdate { if (it > 0) it - 1 else it } > 0) return respond(content = "", status = HttpStatusCode.TooManyRequests)

        if (networkDown) throw IOException("network down")
        val language = request.url.parameters["language"]?.substringBefore('-')

        if (failingEndpoints.any { (endpoint, failingLanguage) -> endpoint.matches(path) && (failingLanguage == null || failingLanguage == language) }) return respond(content = "", status = HttpStatusCode.InternalServerError)

        val fixturePath = when {
            path.startsWith("search/") -> "$path/${slug(request.url.parameters["query"].orEmpty())}"
            else -> path
        }

        val fixture = language?.let { fixture("$fixturePath.$it") } ?: fixture(fixturePath)

        return when {
            fixture != null -> respond(content = fixture, status = HttpStatusCode.OK, headers = JSON_HEADERS)
            path.startsWith("search/") -> respond(content = EMPTY_SEARCH, status = HttpStatusCode.OK, headers = JSON_HEADERS)
            else -> respond(content = NOT_FOUND, status = HttpStatusCode.NotFound, headers = JSON_HEADERS)
        }

    }

    /**
     * Number of calls made to [endpoint], where `{…}` matches any path segment: `movie/{id}`, `tv/{id}/season/{season}`.
     */
    fun callCount(endpoint: String): Int {
        val pattern = Regex(endpoint.split("/").joinToString("/") { if (it.startsWith("{")) "[^/]+" else Regex.escape(it) })
        return calls.count { pattern.matches(it) }
    }

    val totalCallCount: Int get() = calls.size

    /** Holds every response until [resume], to keep a Synchro in progress. */
    fun pause() {
        gate = CompletableDeferred()
    }

    /** Holds only the responses of [endpoint] (see [callCount]) until [resume]. */
    fun pause(endpoint: String) {
        endpointGate = Regex(endpoint.split("/").joinToString("/") { if (it.startsWith("{")) "[^/]+" else Regex.escape(it) }) to CompletableDeferred()
    }

    fun resume() {
        gate?.complete(Unit)
        gate = null
        endpointGate?.second?.complete(Unit)
        endpointGate = null
    }

    /** Makes every request fail like a network error, until [restoreNetwork]. */
    fun cutNetwork() {
        networkDown = true
    }

    fun restoreNetwork() {
        networkDown = false
        failingEndpoints.clear()
    }

    /** Answers [endpoint] (see [callCount]) with a server error, until [restoreNetwork]; only the requests in [language] (`en`) when given. */
    fun failEndpoint(endpoint: String, language: String? = null) {
        failingEndpoints += Regex(endpoint.split("/").joinToString("/") { if (it.startsWith("{")) "[^/]+" else Regex.escape(it) }) to language
    }

    fun resetCalls() = calls.clear()

    private fun fixture(path: String): String? =
        javaClass.classLoader?.getResource("tmdb/$path.json")?.readText()

    private fun slug(query: String): String =
        query.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

}
