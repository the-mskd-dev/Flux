package com.mskd.flux.utils

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.BINARY)
@Preview(
    name = "1. Phone",
    group = "Devices",
    device = Devices.PHONE,
    showBackground = true
)
@Preview(
    name = "2. Foldable",
    group = "Devices",
    device = Devices.FOLDABLE,
    showBackground = true
)
@Preview(
    name = "3. Tablet",
    group = "Devices",
    device = Devices.TABLET,
    showBackground = true
)
annotation class FluxPreview