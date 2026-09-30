package com.mskd.flux.presentation.bottomSheet

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mskd.flux.presentation.FluxUI
import com.mskd.flux.presentation.Text
import com.mskd.flux.presentation.blurForeground
import com.mskd.flux.presentation.fillMaxWidthWithLimit
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.rememberHazeState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluxBottomSheet(
    isVisible: Boolean,
    title: String,
    onDismiss: () -> Unit,
    hazeState: HazeState = rememberHazeState(),
    content: @Composable ColumnScope.() -> Unit,
) {

    BackHandler(enabled = isVisible, onBack = onDismiss)

    Box(modifier = Modifier.fillMaxSize()) {

        // Scrim
        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier.matchParentSize(),
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = .3f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
            )
        }

        // Sheet
        AnimatedVisibility(
            visible = isVisible,
            modifier = Modifier.align(Alignment.BottomCenter),
            enter = slideInVertically { it },
            exit = slideOutVertically { it }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidthWithLimit()
                    .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    .blurForeground(hazeState)
                    .background(BottomSheetDefaults.ContainerColor.copy(alpha = .9f))
                    .navigationBarsPadding()
                    .padding(vertical = FluxUI.Space.medium),
                verticalArrangement = Arrangement.spacedBy(FluxUI.Space.small)
            ) {

                Text.List.Title(
                    modifier = Modifier.padding(horizontal = FluxUI.Space.medium),
                    text = title
                )

                content()

            }
        }

    }

}