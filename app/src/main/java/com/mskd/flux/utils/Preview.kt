package com.mskd.flux.utils

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImagePreviewHandler
import coil3.compose.LocalAsyncImagePreviewHandler
import coil3.imageLoader
import coil3.request.ImageRequest
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.ui.theme.FluxTheme
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_help
import flux.shared.generated.resources.preview_poster
import org.jetbrains.compose.resources.painterResource

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
fun FluxThemePreview(
    customization: CustomizationDataStore.State = CustomizationDataStore.State(uiTheme = UiCommon.THEME.DARK),
    content: @Composable () -> Unit
) {

    val previewHandler = AsyncImagePreviewHandler { request ->
        request.context.imageLoader.execute(
            ImageRequest.Builder(request.context)
                .data(Res.drawable.preview_poster)
                .build()
        ).image!!
    }

    CompositionLocalProvider(LocalAsyncImagePreviewHandler provides previewHandler) {
        FluxTheme(customization = customization) {
            Box(modifier = Modifier.fillMaxSize()) {
                content()
            }
        }
    }

}

@Preview
@Composable
fun Drawable_Preview() {
    FluxTheme {
        Box(
            modifier = Modifier.padding(100.dp),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(Res.drawable.ic_help),
                contentDescription = "preview"
            )

        }
    }
}