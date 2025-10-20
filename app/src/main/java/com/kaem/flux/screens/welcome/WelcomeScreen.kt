package com.kaem.flux.screens.welcome

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.TextMedium
import com.kaem.flux.ui.component.TextTitle
import com.kaem.flux.ui.theme.Ui
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onPermissionsTap: () -> Unit
) {

    val presentations = listOf(
        stringResource(R.string.presentation_1_title) to stringResource(R.string.presentation_1_description),
        stringResource(R.string.presentation_2_title) to stringResource(R.string.presentation_2_description),
    )

    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(0) { presentations.size }
    val backgroundImage = when (pagerState.currentPage) {
        0 -> R.drawable.home_screen
        1 -> R.drawable.media_screen
        else -> R.drawable.search_screen
    }

    BackHandler(enabled = pagerState.currentPage > 0) {
        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {

        val (background, descriptions, buttons) = createRefs()
        val guideline = createGuidelineFromTop(.7f)

        WelcomeBackground(
            modifier = Modifier.constrainAs(background) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(parent.bottom)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
            drawableId = backgroundImage
        )

        WelcomePager(
            modifier = Modifier.constrainAs(descriptions) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(guideline)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                },
            pagerState = pagerState,
            presentations = presentations
        )

        WelcomeButtons(
            modifier = Modifier.constrainAs(buttons) {
                top.linkTo(guideline)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            index = pagerState.currentPage,
            lastIndex = presentations.lastIndex,
            onIndexChange = { scope.launch { pagerState.animateScrollToPage(it) } },
            onPermissionsTap = onPermissionsTap
        )

    }

}

@Composable
fun WelcomeBackground(
    modifier: Modifier,
    drawableId: Int
) {

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {

        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = drawableId,
            label = "background animation"
        ) { id ->

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationY = -15f
                        translationX = 15f
                        rotationZ = -15f
                        rotationX = 15f
                        cameraDistance = 15f
                    },
                painter = painterResource(id),
                contentDescription = "app presentation image"
            )

        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background.copy(.8f),
                            MaterialTheme.colorScheme.background
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    )
                )
        )

    }

}

@Composable
fun WelcomePager(
    modifier: Modifier,
    pagerState: PagerState,
    presentations: List<Pair<String, String>>
) {

    HorizontalPager(
        modifier = modifier,
        state = pagerState,
        pageSize = PageSize.Fill,
        pageContent = { page ->

            val presentation = presentations[page]

            WelcomeItem(
                title = presentation.first,
                description = presentation.second,
                textColor = MaterialTheme.colorScheme.onBackground
            )

        }
    )

}

@Composable
fun WelcomeItem(
    title: String,
    description: String,
    textColor: Color
) {

    Box(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(horizontal = Ui.Space.MEDIUM),
        contentAlignment = Alignment.Center
    ) {

        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
        ) {

            TextTitle(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                color = textColor
            )

            TextMedium(
                modifier = Modifier.fillMaxWidth(),
                text = description,
                color = textColor
            )

        }

    }

}

@Composable
fun WelcomeButtons(
    modifier: Modifier,
    index: Int,
    lastIndex: Int,
    onIndexChange: (Int) -> Unit,
    onPermissionsTap: () -> Unit
) {

    val backVisibility by animateFloatAsState(if (index == 0) 0f else 1f, label = "back animation")

    AnimatedContent(
        modifier = modifier,
        targetState = index == lastIndex,
        label = "welcome buttons anim"
    ) { isLastText ->

        if (isLastText) {

            FluxButton(
                text = stringResource(id = R.string.give_permission),
                onTap = onPermissionsTap
            )

        } else {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(200.dp)
            ) {

                IconButton(
                    modifier = Modifier.alpha(backVisibility),
                    onClick = { if (index != 0) onIndexChange(index - 1) },
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = "Back button"
                        )
                    }
                )

                IconButton(
                    onClick = { onIndexChange(index + 1) },
                    content = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                            tint = MaterialTheme.colorScheme.onBackground,
                            contentDescription = "Next button"
                        )
                    }
                )


            }

        }

    }

}

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun fluxPermissionState(): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_VIDEO
    else
        android.Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}