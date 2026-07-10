package com.mskd.flux.screens.sources.composables


import android.annotation.SuppressLint
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@SuppressLint("ComposableNaming")
@Composable
fun sourcesAnnotatedString(stringRes: StringResource) : AnnotatedString {

    val desc = stringResource(stringRes, Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_DOWNLOADS)
    return buildAnnotatedString {

        append(desc.substringBefore(Environment.DIRECTORY_MOVIES))
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(Environment.DIRECTORY_MOVIES)
        }

        append(desc.substringAfter(Environment.DIRECTORY_MOVIES).substringBefore(Environment.DIRECTORY_DOWNLOADS))
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold)) {
            append(Environment.DIRECTORY_DOWNLOADS)
        }
        append(desc.substringAfter(Environment.DIRECTORY_DOWNLOADS))

    }

}