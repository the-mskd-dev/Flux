package com.kaem.flux.screens.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.hilt.navigation.compose.hiltViewModel
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.kaem.flux.model.flux.FluxMovie
import com.kaem.flux.model.flux.FluxShow
import com.kaem.flux.ui.component.FluxButton
import com.kaem.flux.ui.component.Title
import com.kaem.flux.ui.theme.FluxFontSize
import com.kaem.flux.ui.theme.FluxSpace
import com.kaem.flux.utils.Constants
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date

@Composable
fun DetailsScreen(
    onBackButtonTap: () -> Unit,
    viewModel: DetailsViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(FluxSpace.LARGE)
    ) {

        DetailsHeader(
            imagePath = Constants.TMDB.IMAGE + uiState?.artwork?.bannerPath.orEmpty(),
            artworkTitle = uiState?.artwork?.title.orEmpty(),
            releaseDate = uiState?.artwork?.releaseDate,
            onBackButtonTap = { onBackButtonTap() },
            onLaunchButtonTap = {}
        )

        uiState?.description?.let {
            Text(
                modifier = Modifier.padding(horizontal = FluxSpace.MEDIUM),
                text = it,
                color = MaterialTheme.colorScheme.onBackground,
                fontSize = FluxFontSize.MEDIUM,
                textAlign = TextAlign.Start
            )
        }

    }

}

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun DetailsHeader(
    imagePath: String,
    artworkTitle: String,
    releaseDate: Date?,
    onBackButtonTap: () -> Unit,
    onLaunchButtonTap: () -> Unit
) {

    ConstraintLayout(modifier = Modifier.fillMaxWidth()) {

        val (image, back, title, button, date) = createRefs()

        GlideImage(
            modifier = Modifier
                .constrainAs(image) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                    width = Dimension.fillToConstraints
                }
                .aspectRatio(6f / 5f),
            model = imagePath,
            contentScale = ContentScale.Crop,
            contentDescription = artworkTitle
        )

        Box(
            modifier = Modifier
                .statusBarsPadding()
                .constrainAs(back) {
                    top.linkTo(parent.top)
                    start.linkTo(parent.start, FluxSpace.MEDIUM)
                }
                .size(40.dp)
                .clip(shape = CircleShape)
                .clickable { onBackButtonTap() }
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onBackground,
                    shape = CircleShape
                )
                .background(color = MaterialTheme.colorScheme.background)
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = Icons.Rounded.ArrowBack,
                tint = MaterialTheme.colorScheme.onBackground,
                contentDescription = "back button"
            )

        }

        FluxButton(
            modifier = Modifier
                .scale(1.2f)
                .clip(shape = RoundedCornerShape(.5f))
                .constrainAs(button) {
                    top.linkTo(image.bottom)
                    bottom.linkTo(image.bottom)
                    end.linkTo(parent.end, FluxSpace.MEDIUM)
                },
            text = "Continue",
            onClick = { onLaunchButtonTap() }
        )

        Title(
            modifier = Modifier.constrainAs(title) {
                top.linkTo(button.bottom, FluxSpace.SMALL)
                start.linkTo(parent.start, FluxSpace.MEDIUM)
                end.linkTo(parent.end, FluxSpace.MEDIUM)
                width = Dimension.fillToConstraints
            },
            text = artworkTitle
        )

        releaseDate?.let {
            Text(
                modifier = Modifier
                    .constrainAs(date) {
                        top.linkTo(title.bottom, 4.dp)
                        start.linkTo(title.start)
                    }
                    .alpha(.8f),
                text = DateFormat.getDateInstance().format(it),
                fontSize = FluxFontSize.SMALL,
                color = MaterialTheme.colorScheme.onBackground,
                fontStyle = FontStyle.Italic
            )
        }

    }

}