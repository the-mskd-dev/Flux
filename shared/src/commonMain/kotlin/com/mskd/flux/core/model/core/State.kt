package com.mskd.flux.core.model.core

sealed class State<out T> {
    data class Content<T>(val content: T) : State<T>()
    data object Loading : State<Nothing>()
    data class Error(val code: Int? = null, val message: StringProvider? = null) : State<Nothing>()
}