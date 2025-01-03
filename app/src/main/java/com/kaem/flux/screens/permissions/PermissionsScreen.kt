package com.kaem.flux.screens.permissions

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.room.util.TableInfo
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.rememberPermissionState
import com.kaem.flux.R
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.MediumText
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.Ui

@Composable
fun PermissionsScreen(
    onPermissionsTap: () -> Unit
) {

    var index by remember { mutableIntStateOf(0) }
    val backVisibility by animateFloatAsState(if (index == 0) 0f else 1f, label = "back animation")

    BackHandler(enabled = index > 0) {
        index--
    }

    ConstraintLayout(
        modifier = Modifier.fillMaxSize()
    ) {

        val (texts, buttons) = createRefs()
        val guideline = createGuidelineFromTop(.7f)

        AnimatedContent(
            modifier = Modifier
                .statusBarsPadding()
                .constrainAs(texts) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    bottom.linkTo(guideline)
                    height = Dimension.fillToConstraints
                    width = Dimension.fillToConstraints
                },
            targetState = index,
            label = "textsAnim"
        ) { i ->

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(Ui.Space.LARGE)
            ) {

                val presentation = presentations[i]

                Title(
                    modifier = Modifier.fillMaxWidth(),
                    text = presentation.title
                )

                MediumText(
                    modifier = Modifier.fillMaxWidth(),
                    text = presentation.description
                )

            }

        }

        AnimatedContent(
            modifier = Modifier.constrainAs(buttons) {
                top.linkTo(guideline, Ui.Space.LARGE)
                start.linkTo(parent.start)
                end.linkTo(parent.end)
            },
            targetState = index == presentations.lastIndex
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
                        onClick = { if (index != 0) index-- },
                        content = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                tint = MaterialTheme.colorScheme.onBackground,
                                contentDescription = "Back button"
                            )
                        }
                    )

                    IconButton(
                        onClick = { index++ },
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

}

data class Presentation(val title: String, val description: String)

val presentations = listOf(
    Presentation(
        title = "Titre 1",
        description = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum."
    ),
    Presentation(
        title = "Titre 2",
        description = "Sed ut perspiciatis unde omnis iste natus error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. "
    ),
    Presentation(
        title = "Titre 3",
        description = "At vero eos et accusamus et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque corrupti quos dolores et quas molestias excepturi sint occaecati cupiditate non provident,"
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