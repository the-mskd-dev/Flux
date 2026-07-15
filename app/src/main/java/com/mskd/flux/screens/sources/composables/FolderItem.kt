package com.mskd.flux.screens.sources.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.model.cleanPath
import com.mskd.flux.features.sources.domain.model.name
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.ui.component.global.DraggableItem
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.delete
import flux.shared.generated.resources.downloads
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.ic_error
import flux.shared.generated.resources.ic_lock
import flux.shared.generated.resources.movies
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderItem(
    name: String,
    path: String? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    icon: @Composable () -> Unit = {}
) {

    Row(
        modifier = Modifier
            .clip(FluxUI.shapes.corners)
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(all = FluxUI.Space.medium),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
    ) {

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
        ) {

            Text.Title.Medium(
                text = name,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                color = contentColor,
            )

            Text.Adaptive(
                text = path,
                overflow = TextOverflow.StartEllipsis,
                maxLines = 1,
                color = contentColor.copy(alpha = .6f),
                style = MaterialTheme.typography.titleSmall.copy(fontStyle = FontStyle.Italic)
            )

        }

        icon()


    }



}

@Composable
fun PermanentFolderItem(
    modifier: Modifier = Modifier,
    name: String
) {

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = FluxUI.Space.medium),
        contentAlignment = Alignment.Center
    ) {

        FolderItem(
            name = name,
            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.ic_lock),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    contentDescription = name
                )
            }
        )

    }

}

@Composable
fun UserFolderItem(
    modifier: Modifier = Modifier,
    folder: UserFolder,
    onDelete: () -> Unit
) {

    DraggableItem(
        modifier = modifier,
        content = {

            FolderItem(
                name = folder.name,
                path = folder.cleanPath,
                backgroundColor = if (!folder.isAvailable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primaryContainer.copy(alpha = .5f),
                contentColor = if (!folder.isAvailable) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {

                    if (!folder.isAvailable) {
                        Icon(
                            tint = MaterialTheme.colorScheme.errorContainer,
                            painter = painterResource(Res.drawable.ic_error),
                            contentDescription = folder.name
                        )
                    }
                }
            )

        },
        actionContent = {

            Icon(
                tint = MaterialTheme.colorScheme.onErrorContainer,
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = stringResource(Res.string.delete)
            )

        },
        onActionTap = { onDelete() },
    )

}

@Composable
@Preview
fun FolderItem_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(vertical = FluxUI.Space.large),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {
            PermanentFolderItem(name = stringResource(Res.string.movies))
            PermanentFolderItem(name = stringResource(Res.string.downloads))
            FilesMockups.userFolders.forEach { UserFolderItem(folder = it, onDelete = {}) }
        }
    }
}