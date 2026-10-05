package com.mskd.flux.features.setup.ui.composables

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.constraintlayout.compose.platform.annotation.SuppressLint
import org.jetbrains.compose.resources.StringResource

@SuppressLint("ComposableNaming")
@Composable
expect fun sourcesAnnotatedString(stringRes: StringResource) : AnnotatedString