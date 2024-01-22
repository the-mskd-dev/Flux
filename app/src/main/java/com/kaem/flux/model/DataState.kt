package com.kaem.flux.model

sealed  class DataState<T> {
    class Loading<T> : DataState<T>()
    data class Success<T>(val data: List<T>) : DataState<T>()
    data class Error<T>(val message: String) : DataState<T>()

}