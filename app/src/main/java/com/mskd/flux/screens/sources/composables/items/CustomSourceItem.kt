package com.mskd.flux.screens.sources.composables.items

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mskd.flux.features.sources.domain.model.UserFolder
import com.mskd.flux.features.sources.domain.model.cleanPath
import com.mskd.flux.features.sources.domain.model.name
import com.mskd.flux.ui.theme.FluxUI
import com.mskd.flux.utils.FluxThemePreview
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.ic_error
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource

@Composable
fun LazyItemScope.CustomSourceItem(
    modifier: Modifier = Modifier,
    folder: UserFolder,
    onDelete: () -> Unit
) {

    val scope = rememberCoroutineScope()

    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
    )

    val isSwiping by remember {
        derivedStateOf {
            runCatching { dismissState.requireOffset() != 0f }.getOrDefault(false)
        }
    }

    val cornersAnimation by animateDpAsState(
        if (isSwiping) FluxUI.shapes.listItem else 0.dp
    )

    LaunchedEffect(folder.path) {
        dismissState.snapTo(SwipeToDismissBoxValue.Settled)
    }

    val backgroundColor = if (!folder.isAvailable) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondaryContainer
    val contentColor = if (!folder.isAvailable) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onSecondaryContainer

    SwipeToDismissBox(
        modifier = Modifier
            .animateItem()
            .fillMaxWidth(),
        state = dismissState,
        enableDismissFromStartToEnd = false,
        onDismiss = {
            scope.launch { dismissState.snapTo(SwipeToDismissBoxValue.Settled) }
            onDelete()
        },
        content = {
            ListItem(
                modifier = modifier
                    .clip(RoundedCornerShape(cornersAnimation)),
                colors = ListItemDefaults.colors(
                    containerColor = backgroundColor,
                    contentColor = contentColor
                ),
                headlineContent = {
                    Text(
                        text = folder.name,
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                    )
                },
                supportingContent = {
                    Text(
                        text = folder.cleanPath,
                        fontSize = 14.sp,
                        lineHeight = 20.sp,
                        overflow = TextOverflow.StartEllipsis,
                        maxLines = 1,
                    )
                },
                trailingContent = {
                    if (!folder.isAvailable) {
                        Icon(
                            tint = contentColor,
                            painter = painterResource(Res.drawable.ic_error),
                            contentDescription = folder.name
                        )
                    }
                }
            )
        },
        backgroundContent = {
            when (dismissState.dismissDirection) {
                SwipeToDismissBoxValue.EndToStart -> {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove item",
                        modifier = modifier
                            .clip(RoundedCornerShape(cornersAnimation))
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.errorContainer)
                            .wrapContentSize(Alignment.CenterEnd)
                            .padding(12.dp),
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                else -> {}
            }
        }
    )

}

@Preview
@Composable
fun CustomSourceItem_Preview() {
    FluxThemePreview {
        LazyColumn(
            modifier = Modifier
                .padding(FluxUI.Space.medium)
                .clip(FluxUI.shapes.corners),
            verticalArrangement = Arrangement.spacedBy(FluxUI.Space.extraSmall)
        ) {

            item {
                CustomSourceItem(
                    folder = UserFolder(path = "path/to/folder",),
                    onDelete = {}
                )
            }

            item {
                CustomSourceItem(
                    folder = UserFolder(path = "path/to/folder2",),
                    onDelete = {}
                )
            }

            item {
                CustomSourceItem(
                    folder = UserFolder(path = "path/to/unavailableFolder", isAvailable = false),
                    onDelete = {},
                )
            }

        }
    }
}