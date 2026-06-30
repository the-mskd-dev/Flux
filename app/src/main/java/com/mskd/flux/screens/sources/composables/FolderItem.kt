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
import androidx.compose.ui.tooling.preview.Preview
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.name

@Composable
fun FolderItem(folder: UserFolder) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FluxUI.shapes.corners)
            .background(if (folder.status == UserFolder.Status.MISSING) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.background )
            .padding(all = FluxUI.Space.medium),
        verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
    ) {

        Text.Title.Medium(
            text = folder.name,
            color = MaterialTheme.colorScheme.onBackground
        )

        Text.Title.Small(
            text = folder.path,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f)
        )

    }


}

@Composable
@Preview
fun FolderItem_Preview() {
    FluxTheme {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {
            FilesMockups.userFolders.forEach { FolderItem(folder = it) }
        }
    }
}