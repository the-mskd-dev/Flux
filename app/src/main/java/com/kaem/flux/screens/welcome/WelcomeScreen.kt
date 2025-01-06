package com.kaem.flux.screens.welcome

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.Ui

@Composable
fun WelcomeScreen(
    onPermissionsTap: () -> Unit
) {

    var index by remember { mutableIntStateOf(0) }

    val backgroundColor by animateColorAsState(
        targetValue = when (index) {
            0 -> MaterialTheme.colorScheme.primary
            1 -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.primaryContainer
        },
        label = "backgroundColor"
    )

    val textColor by animateColorAsState(
        targetValue = when (index) {
            0 -> MaterialTheme.colorScheme.onPrimary
            1 -> MaterialTheme.colorScheme.onSecondaryContainer
            else -> MaterialTheme.colorScheme.onPrimaryContainer
        },
        label = "backgroundColor"
    )

    BackHandler(enabled = index > 0) {
        index--
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
                bottom.linkTo(guideline)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
            backgroundColor = backgroundColor
        )

        WelcomeDescriptions(
            modifier = Modifier.constrainAs(descriptions) {
                top.linkTo(parent.top)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
                bottom.linkTo(guideline)
                height = Dimension.fillToConstraints
                width = Dimension.fillToConstraints
            },
            textColor = textColor,
            index = index
        )

        WelcomeButtons(
            modifier = Modifier.constrainAs(buttons) {
                top.linkTo(guideline)
                bottom.linkTo(parent.bottom)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            index = index,
            onIndexChange = { index = it },
            onPermissionsTap = onPermissionsTap
        )

    }

}

@Composable
fun WelcomeBackground(
    modifier: Modifier,
    backgroundColor: Color
) {

    Box(
        modifier = modifier
            .background(brush = Brush.verticalGradient(
                colors = listOf(
                    backgroundColor,
                    backgroundColor.copy(alpha = .9f),
                    backgroundColor.copy(alpha = .6f),
                    backgroundColor.copy(alpha = .3f),
                    Color.Transparent,
                ),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
            ))
    )

}

@Composable
fun WelcomeDescriptions(
    modifier: Modifier,
    textColor: Color,
    index: Int
) {

    AnimatedContent(
        modifier = modifier,
        targetState = index,
        label = "textsAnim",
        transitionSpec = {
            if (targetState > initialState) {
                slideInHorizontally { width -> width } togetherWith slideOutHorizontally { width -> -width }
            } else {
                slideInHorizontally { width -> -width }  togetherWith slideOutHorizontally { width -> width }
            }
        }
    ) { i ->

        val presentation = presentations[i]

        WelcomeItem(
            modifier = Modifier,
            title = presentation.title,
            description = presentation.description,
            textColor = textColor
        )

    }

}

@Composable
fun WelcomeItem(
    modifier: Modifier,
    title: String,
    description: String,
    textColor: Color
) {

    Column(
        modifier = modifier
            .background(brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primary.copy(alpha = .9f),
                    MaterialTheme.colorScheme.primary.copy(alpha = .6f),
                    MaterialTheme.colorScheme.primary.copy(alpha = .3f),
                    Color.Transparent,
                ),
                startY = 0f,
                endY = Float.POSITIVE_INFINITY
            ))
            .statusBarsPadding()
            .fillMaxSize()
            .padding(start = Ui.Space.MEDIUM, end = Ui.Space.MEDIUM, top = Ui.Space.LARGE),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
    ) {

        Title(
            modifier = Modifier.fillMaxWidth(),
            text = title,
            color = textColor
        )

        MediumText(
            modifier = Modifier.fillMaxWidth(),
            text = description,
            color = textColor
        )

    }

}

@Composable
fun WelcomeButtons(
    modifier: Modifier,
    index: Int,
    onIndexChange: (Int) -> Unit,
    onPermissionsTap: () -> Unit
) {

    val backVisibility by animateFloatAsState(if (index == 0) 0f else 1f, label = "back animation")

    AnimatedContent(
        modifier = modifier,
        targetState = index == presentations.lastIndex,
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

data class Presentation(val title: String, val description: String)

val presentations = listOf(
    Presentation(
        title = "Bienvenue dans Flux",
        description = "Retrouvez vos films, séries et animes, dans une librairie simple et organisée"
    ),
    Presentation(
        title = "Accès aux données",
        description = "Pour le fonctionnement de l'application, nous avons besoin d'avoir accès à vos fichiers vidéos. Aucune donnée personnelle n'est récupérée ni transmise à quiconque"
    )
)

@Composable
@OptIn(ExperimentalPermissionsApi::class)
fun fluxPermissionState(): PermissionState {

    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        android.Manifest.permission.READ_MEDIA_VIDEO
    else
        android.Manifest.permission.READ_EXTERNAL_STORAGE

    return rememberPermissionState(permission = permission)

}