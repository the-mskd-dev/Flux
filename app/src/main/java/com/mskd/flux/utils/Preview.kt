package com.mskd.flux.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.imageLoader
import coil3.request.ImageRequest
import com.mskd.flux.R
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "1. Phone",
    group = "Devices",
    device = Devices.PIXEL_9_PRO_XL,
    showBackground = true
)
@Preview(
    name = "2. Foldable",
    group = "Devices",
    device = Devices.PIXEL_9_PRO_FOLD,
    showBackground = true
)
@Preview(
    name = "3. Tablet",
    group = "Devices",
    device = Devices.PIXEL_TABLET,
    showBackground = true
)
annotation class FluxPreview

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "1. Pixel 9 PRO XL",
    group = "Devices",
    device = Devices.PIXEL_9_PRO_XL,
    showBackground = true
)
@Preview(
    name = "2. Pixel 5",
    group = "Devices",
    device = Devices.PIXEL_5,
    showBackground = true
)
annotation class PortraitPreview

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "1. Phone Landscape",
    showBackground = true,
    device = "spec:parent=pixel_9_pro_xl,orientation=landscape"
)
@Preview(
    name = "2. Foldable",
    group = "Devices",
    device = Devices.PIXEL_9_PRO_FOLD,
    showBackground = true
)
@Preview(
    name = "3. Tablet",
    group = "Devices",
    device = Devices.PIXEL_TABLET,
    showBackground = true
)
annotation class LandscapePreview


@OptIn(ExperimentalCoilApi::class)
@Composable
fun AppThemePreview(
    theme: Ui.THEME = Ui.THEME.SYSTEM,
    color: Int? = null,
    content: @Composable () -> Unit
) {

    val previewHandler = AsyncImagePreviewHandler { request ->
        request.context.imageLoader.execute(
            ImageRequest.Builder(request.context)
                .data(R.drawable.preview_poster)
                .build()
        ).image!!
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        AppTheme(theme = theme, color = color) {
            content()
        }
    }

}