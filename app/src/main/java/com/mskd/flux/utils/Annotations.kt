package com.mskd.flux.utils

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

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