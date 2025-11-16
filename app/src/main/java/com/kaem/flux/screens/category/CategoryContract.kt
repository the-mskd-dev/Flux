package com.kaem.flux.screens.category

sealed class CategoryIntent {
    object OnBackTap: CategoryIntent()
    data class OnMediaTap(val mediaId: Long): CategoryIntent()
}

sealed class CategoryEvent {
    object BackToPreviousScreen : CategoryEvent()
    data class NavigateToMedia(val mediaId: Long) : CategoryEvent()
}