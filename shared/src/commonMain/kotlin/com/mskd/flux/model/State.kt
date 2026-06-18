package com.mskd.flux.model

sealed class State<out T> {
    data object Loading : State<Nothing>()
    data object Error : State<Nothing>()
    data class Content<T>(val content: T) : State<T>()
}