package com.mskd.flux.screens.catalog.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.FluxThemePreview
import com.mskd.flux.utils.extensions.fillMaxWidthWithLimit
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_close
import flux.shared.generated.resources.ic_visibility
import flux.shared.generated.resources.learn_more
import flux.shared.generated.resources.soon_play_store
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource


// TODO : Delete ASAP
@Composable
fun PlayStoreMessage() {

    Row(
        modifier = Modifier
            .fillMaxWidthWithLimit()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.tertiary)
            .padding(vertical = FluxUI.Space.small, horizontal = FluxUI.Space.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
    ) {

        val text = buildAnnotatedString {

            append(stringResource(Res.string.soon_play_store))
            append(" ")

            val linkAnnotation = LinkAnnotation.Clickable(
                tag = "LEARN_MORE",
                styles = TextLinkStyles(
                    style = SpanStyle(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                ),
                linkInteractionListener = {  }
            )

            withLink(linkAnnotation) {
                append(stringResource(Res.string.learn_more))
            }

        }

        Text.Annotated(
            modifier = Modifier.weight(1f),
            text = text,
            style = Text.Style.contentBody(),
            color = MaterialTheme.colorScheme.onTertiary,
        )

        IconButton(
            onClick = {}
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_close),
                tint = MaterialTheme.colorScheme.onPrimary,
                contentDescription = "close"
            )
        }
    }
}

@FluxPreview
@Composable
fun PlayStoreMessage_Preview() {
    FluxThemePreview {
        PlayStoreMessage()
    }
}