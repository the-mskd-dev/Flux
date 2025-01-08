package com.kaem.flux.ui.component

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.Placeholder
import com.bumptech.glide.integration.compose.placeholder

object Placeholders {

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun loading() : Placeholder = placeholder(ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f)))

    @OptIn(ExperimentalGlideComposeApi::class)
    @Composable
    fun failure() : Placeholder = placeholder(ColorPainter(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = .4f)))

}