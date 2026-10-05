package com.mskd.flux.core.model.core

import com.mskd.flux.ui.text.TextProvider

sealed class State<out T> {
    data class Content<T>(val content: T) : State<T>()
    data object Loading : State<Nothing>()
    data class Error(val code: Int? = null, val message: TextProvider? = null) : State<Nothing>()
}