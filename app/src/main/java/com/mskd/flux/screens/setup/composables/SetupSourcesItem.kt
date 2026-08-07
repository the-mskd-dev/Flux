package com.mskd.flux.screens.setup.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.screens.sources.composables.sourcesAnnotatedString
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.setup_sources_default_desc
import flux.shared.generated.resources.setup_sources_default_title
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun SetupSourcesItem(
    title: String,
    description: AnnotatedString,
    isSelected: Boolean,
    onClick: () -> Unit
) {

    ListItem(
        selected = isSelected,
        onClick = onClick,
        content = {
            Text.List.Title(text = title)
        },
        supportingContent = {
            Text.Annotated(
                text = description,
                style = Text.Style.listBody()
            )
        },
    )

}

@Preview
@Composable
fun SetupSourcesItem_Selected_Preview() {
    FluxThemePreview {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.background)
                .padding(all = FluxUI.Space.medium)
            ,
            contentAlignment = Alignment.Center
        ) {
            SetupSourcesItem(
                title = stringResource(Res.string.setup_sources_default_title),
                description = sourcesAnnotatedString(Res.string.setup_sources_default_desc),
                isSelected = true,
                onClick = {  }
            )
        }
    }
}

@Preview
@Composable
fun SetupSourcesItem_Unselected_Preview() {
    FluxThemePreview {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.surfaceContainer)
                .padding(all = FluxUI.Space.medium)
            ,
            contentAlignment = Alignment.Center
        ) {
            SetupSourcesItem(
                title = stringResource(Res.string.setup_sources_default_title),
                description = sourcesAnnotatedString(Res.string.setup_sources_default_desc),
                isSelected = false,
                onClick = {  }
            )
        }
    }
}