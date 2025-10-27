package com.kaem.flux.screens.category

sealed class CategoryEvent {
    object BackToPreviousScreen : CategoryEvent()
    data class NavigateToMedia(val mediaId: Long) : CategoryEvent()
}