package com.mskd.flux.features.catalog.sync

import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.CompletableDeferred
import java.io.IOException
import java.util.concurrent.CopyOnWriteArrayList

/**
 * TMDB simulated at the HTTP level: each request is answered with the JSON fixture found at
 * `tmdb/<path>.json` in the test resources, and recorded to count the calls per endpoint.
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
    private var networkDown = false

    private val failingEndpoints = CopyOnWriteArrayList<Regex>()

    val engine = MockEngine { request ->

        val path = request.url.encodedPath.removePrefix(BASE_PATH)
        calls += path
        gate?.await()

        if (networkDown) throw IOException("network down")
        if (failingEndpoints.any { it.matches(path) }) return@MockEngine respond(content = "", status = HttpStatusCode.InternalServerError)

        val fixturePath = when {
            path.startsWith("search/") -> "$path/${slug(request.url.parameters["query"].orEmpty())}"
            else -> path
        }

        val fixture = fixture(fixturePath)

        when {
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

    fun resume() {
        gate?.complete(Unit)
        gate = null
    }

    /** Makes every request fail like a network error, until [restoreNetwork]. */
    fun cutNetwork() {
        networkDown = true
    }

    fun restoreNetwork() {
        networkDown = false
        failingEndpoints.clear()
    }

    /** Answers [endpoint] (see [callCount]) with a server error, until [restoreNetwork]. */
    fun failEndpoint(endpoint: String) {
        failingEndpoints += Regex(endpoint.split("/").joinToString("/") { if (it.startsWith("{")) "[^/]+" else Regex.escape(it) })
    }

    fun resetCalls() = calls.clear()

    private fun fixture(path: String): String? =
        javaClass.classLoader?.getResource("tmdb/$path.json")?.readText()

    private fun slug(query: String): String =
        query.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')

}
