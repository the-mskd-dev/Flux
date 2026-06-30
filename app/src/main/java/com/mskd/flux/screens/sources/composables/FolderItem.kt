package com.mskd.flux.screens.sources.composables

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.name
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.downloads
import flux.shared.generated.resources.movies
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderItem(
    name: String,
    path: String? = null,
    background: Color
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FluxUI.shapes.corners)
            .background(background)
            .padding(all = FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        Text.Title.Medium(
            text = name,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text.Title.Small(
            text = path,
            overflow = TextOverflow.StartEllipsis,
            maxLines = 1,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f)
        )

    }

}

@Composable
fun PermanentFolderItem(name: String) {

    FolderItem(
        name = name,
        background = MaterialTheme.colorScheme.primaryContainer
    )

}

@Composable
fun UserFolderItem(folder: UserFolder) {

    FolderItem(
        name = folder.name,
        path = folder.path,
        background = if (folder.status == UserFolder.Status.MISSING) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background
    )

}

@Composable
@Preview
fun FolderItem_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {
            PermanentFolderItem(name = stringResource(Res.string.movies))
            PermanentFolderItem(name = stringResource(Res.string.downloads))
            FilesMockups.userFolders.forEach { UserFolderItem(folder = it) }
        }
    }
}