package com.mskd.flux.features.season.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mskd.flux.features.season.presentation.SeasonContent
import com.mskd.flux.features.season.presentation.SeasonPageDialog
import com.mskd.flux.features.season.presentation.SeasonIntent
import com.mskd.flux.ui.FluxPreview
import com.mskd.flux.ui.components.FluxScaffold
import com.mskd.flux.ui.components.ResetProgressDialog
import com.mskd.flux.ui.dimensions.rememberScreenDimensions
import com.mskd.flux.ui.modal.dialog.FluxDialog
import com.mskd.flux.ui.modal.menu.FluxPopUpMenu
import com.mskd.flux.ui.modal.menu.FluxPopUpMenuItem
import com.mskd.flux.ui.text.Text
import com.mskd.flux.ui.theme.FluxTheme
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_eraser
import flux.shared.generated.resources.mark_previous_episodes_as_watched
import flux.shared.generated.resources.more_info
import flux.shared.generated.resources.reset_progress
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun SeasonScreenContent(
    content: SeasonContent,
    sendIntent: (SeasonIntent) -> Unit,
) {

    val isLargeScreen = rememberScreenDimensions().isLarge
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showMenu by remember { mutableStateOf(false) }

    FluxScaffold(
        // The large layout shows the title beside the image
        title = if (isLargeScreen) null else content.title,
        animatedTitle = true,
        actions = {
            IconButton(
                onClick = { showMenu = true },
                content = {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        tint = MaterialTheme.colorScheme.onBackground,
                        contentDescription = "menu button"
                    )
                }
            )

            SeasonDropDownMenu(
                isVisible = showMenu,
                onDismissRequest = { showMenu = false },
                sendIntent = sendIntent
            )
        },
        onBackTap = { sendIntent(SeasonIntent.OnBackTap) },
        scrollBehavior = scrollBehavior
    ) { innerPadding ->

        if (isLargeScreen) {
            SeasonContentLarge(content = content, scaffoldInnerPadding = innerPadding, sendIntent = sendIntent)
        } else {
            SeasonContentRegular(content = content, scaffoldInnerPadding = innerPadding, sendIntent = sendIntent)
        }

    }

    FluxDialog(
        isVisible = content.dialog is SeasonPageDialog.EpisodeStatusConfirmation,
        content = {
            Text.Content.Body(text = stringResource(Res.string.mark_previous_episodes_as_watched))
        },
        onDismiss = { sendIntent(SeasonIntent.CloseDialog) },
        onValidate = { sendIntent(SeasonIntent.MarkPreviousEpisodesAsWatched) }
    )

    if (content.dialog is SeasonPageDialog.ResetProgressConfirmation) {
        ResetProgressDialog(
            onValidate = { sendIntent(SeasonIntent.ResetProgress) },
            onDismiss = { sendIntent(SeasonIntent.CloseDialog) }
        )
    }

}

@Composable
fun SeasonDropDownMenu(
    isVisible: Boolean,
    onDismissRequest: () -> Unit,
    sendIntent: (SeasonIntent) -> Unit
) {

    FluxPopUpMenu(
        isVisible = isVisible,
        onDismissRequest = onDismissRequest,
        items = listOf(
            FluxPopUpMenuItem(
                text = stringResource(Res.string.more_info),
                onClick = {
                    sendIntent(SeasonIntent.OpenSeasonInfo)
                    onDismissRequest()
                },
                leadingIcon = { Icon(imageVector = Icons.Outlined.Info, contentDescription = stringResource(Res.string.more_info)) },
            ),
            FluxPopUpMenuItem(
                text = stringResource(Res.string.reset_progress),
                onClick = {
                    sendIntent(SeasonIntent.ShowResetProgressDialog)
                    onDismissRequest()
                },
                leadingIcon = { Icon(painter = painterResource(Res.drawable.ic_eraser), contentDescription = stringResource(Res.string.reset_progress)) },
            ),
        )
    )

}

@FluxPreview
@Composable
fun SeasonScreenContent_Preview() {
    FluxTheme {
        SeasonScreenContent(content = previewSeasonContent, sendIntent = {})
    }
}
