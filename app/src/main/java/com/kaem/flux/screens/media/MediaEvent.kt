package com.kaem.flux.screens.media

import com.kaem.flux.screens.category.CategoryEvent

sealed class MediaEvent {
    object BackToPreviousScreen : MediaEvent()
}