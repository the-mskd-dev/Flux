package com.kaem.flux.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import com.kaem.flux.R

@Composable
@ReadOnlyComposable
fun buildLinkedString(
    template: String,
    vararg links: Pair<String, String>
): AnnotatedString = buildAnnotatedString {
    val parts = template.split(Regex("%\\d+\\\$s"))
    parts.forEachIndexed { index, part ->
        append(part)
        if (index < links.size) {
            val (label, url) = links[index]
            withLink(LinkAnnotation.Url(url)) { append(label) }
        }
    }
}