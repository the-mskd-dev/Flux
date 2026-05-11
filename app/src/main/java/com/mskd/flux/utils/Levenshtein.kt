package com.mskd.flux.utils

object Levenshtein {

    fun distance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                dp[i][j] = if (a[i - 1] == b[j - 1]) dp[i - 1][j - 1]
                else 1 + minOf(dp[i - 1][j], dp[i][j - 1], dp[i - 1][j - 1])
            }
        }
        return dp[a.length][b.length]
    }

    fun minDistance(query: String, title: String, originalTitle: String): Int {
        val normalized = query.lowercase().trim()
        val distanceLocal = distance(normalized, title.lowercase().trim())
        val distanceOriginal = distance(normalized, originalTitle.lowercase().trim())
        return minOf(distanceLocal, distanceOriginal)
    }

}