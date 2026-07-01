package com.mskd.flux.screens.sources.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.mskd.flux.mockups.FilesMockups
import com.mskd.flux.model.core.presentation.SwipeAnchor
import com.mskd.flux.model.domain.files.UserFolder
import com.mskd.flux.ui.component.global.Text
import com.mskd.flux.ui.theme.FluxTheme
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.extensions.name
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.downloads
import flux.shared.generated.resources.ic_delete
import flux.shared.generated.resources.movies
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun FolderItem(
    name: String,
    path: String? = null,
    background: Color
) {

    val density = LocalDensity.current
    val actionWidth = 24.dp + FluxUI.Space.medium.times(2)
    val actionWidthPx = with(density) { actionWidth.toPx() }
    val anchors = DraggableAnchors {
        SwipeAnchor.OPEN at -actionWidthPx
        SwipeAnchor.CLOSED at 0f
    }
    val state = remember {
        AnchoredDraggableState(
            initialValue = SwipeAnchor.CLOSED,
            anchors = anchors
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {

        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(horizontal = FluxUI.Space.medium)
                .clip(FluxUI.shapes.corners)
                .background(color = MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd
        ) {

            Icon(
                modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                painter = painterResource(Res.drawable.ic_delete),
                contentDescription = "Delete"
            )

        }

        Column(
            modifier = Modifier
                .anchoredDraggable(
                    state = state,
                    orientation = Orientation.Horizontal
                )
                .offset { IntOffset(x = state.requireOffset().toInt(), y = 0) }
                .padding(horizontal = FluxUI.Space.medium)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = FluxUI.Space.large),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.medium)
        ) {
            PermanentFolderItem(name = stringResource(Res.string.movies))
            PermanentFolderItem(name = stringResource(Res.string.downloads))
            FilesMockups.userFolders.forEach { UserFolderItem(folder = it) }
        }
    }
}