package com.kaem.flux.screens.category

import com.kaem.flux.screens.home.HomeIntent

sealed class CategoryIntent {
    object OnBackTap: CategoryIntent()
    data class OnMediaTap(val mediaId: Long): CategoryIntent()
}