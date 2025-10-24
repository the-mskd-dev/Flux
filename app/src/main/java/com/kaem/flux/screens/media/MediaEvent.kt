package com.kaem.flux.screens.media

sealed class MediaEvent {
    object BackToPreviousScreen : MediaEvent()
}