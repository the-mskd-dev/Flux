package com.mskd.flux.model

sealed class State<out T> {
    data class Content<T>(val content: T) : State<T>()
    data object Loading : State<Nothing>()
    data class Error(val code: Int? = null, val message: String? = null) : State<Nothing>() {

        val description: String? get() = when {
            code != null && message != null -> "CODE ${code} (${message})"
            code != null -> "CODE ${code}"
            message != null -> message
            else -> null
        }

    }
}