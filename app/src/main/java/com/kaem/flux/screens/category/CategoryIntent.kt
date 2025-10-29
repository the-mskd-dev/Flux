package com.kaem.flux.screens.category

sealed class CategoryIntent {
    object OnBackTap: CategoryIntent()
    data class OnMediaTap(val mediaId: Long): CategoryIntent()
}