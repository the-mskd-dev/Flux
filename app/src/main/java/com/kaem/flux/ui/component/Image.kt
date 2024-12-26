package com.kaem.flux.ui.component

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.Placeholder
import com.bumptech.glide.integration.compose.placeholder

object Placeholders {

    @OptIn(ExperimentalGlideComposeApi::class)
    val loading: Placeholder = placeholder(ColorPainter(Color.LightGray))

}