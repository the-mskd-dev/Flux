package com.mskd.flux.screens.settings

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mskd.flux.R
import com.mskd.flux.navigation.Route
import com.mskd.flux.ui.component.FluxDialog
import com.mskd.flux.ui.component.FluxScaffold
import com.mskd.flux.ui.component.Text
import com.mskd.flux.ui.theme.AppTheme
import com.mskd.flux.ui.theme.Ui
import com.mskd.flux.utils.Constants
import com.mskd.flux.utils.FluxPreview
import com.mskd.flux.utils.WebLink
import com.mskd.flux.utils.extensions.uppercaseFirstLetter

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    navigate: (Route) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val appVersion = context
        .packageManager
        .getPackageInfo(context.packageName, 0)
        .versionName

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SettingsEvent.BackToPreviousScreen -> onBack()
                SettingsEvent.NavigateToTokenScreen -> navigate(Route.Token(fromSettings = true))
                SettingsEvent.NavigateToAboutScreen -> navigate(Route.About)
                SettingsEvent.NavigateToHowToScreen -> navigate(Route.HowTo)
            }
        }
    }

    SettingsContent(
        state = state,
        context = context,
        appVersion = appVersion,
        sendIntent = viewModel::handleIntent
    )

    state.dialogState?.let {
        SettingsDialog(
            state = it,
            sendIntent = viewModel::handleIntent,
            onDismiss = { viewModel.handleIntent(SettingsIntent.HideDialog) }
        )
    }

}

@Composable
fun SettingsContent(
    state: SettingsUiState,
    context: Context,
    appVersion: String?,
    sendIntent: (SettingsIntent) -> Unit
) {

    FluxScaffold(
        title = stringResource(R.string.settings),
        onBackTap = { sendIntent(SettingsIntent.OnBackTap) }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
        ) {

            Spacer(modifier = Modifier.height(innerPadding.calculateTopPadding()))

            SettingsSection(
                iconColor = MaterialTheme.colorScheme.onTertiaryContainer,
                backgroundColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 1f)
            ) { iconColor, bgColor ->

                SettingsItem(
                    text = stringResource(R.string.app_theme),
                    value = stringResource(state.uiTheme.stringResourceId),
                    painter = painterResource(R.drawable.ic_theme),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = { sendIntent(SettingsIntent.ShowThemeDialog) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.button_rewind),
                    value = "${state.rewindValue}sec",
                    painter = painterResource(R.drawable.fast_rewind),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = { sendIntent(SettingsIntent.ShowRewindDialog) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.button_forward),
                    value = "${state.forwardValue}sec",
                    painter = painterResource(R.drawable.fast_forward),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = { sendIntent(SettingsIntent.ShowForwardDialog) }
                )

                SettingsDivider()

                SettingsSwitch(
                    text = stringResource(R.string.external_player),
                    subText = stringResource(R.string.watch_on_external_player),
                    checked = state.useExternalPlayer,
                    painter = painterResource(R.drawable.ic_player),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onCheckedChange = { sendIntent(SettingsIntent.OnExternalPlayerCheck(it)) }
                )

            }

            SettingsSection(
                iconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                backgroundColor = MaterialTheme.colorScheme.primaryContainer
            ) { iconColor, bgColor ->

                SettingsItem(
                    text = stringResource(R.string.tmdb_api_token),
                    value = "",
                    painter = painterResource(R.drawable.ic_api),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = { sendIntent(SettingsIntent.OnTokenTap) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.how_to_name_files),
                    value = "",
                    painter = painterResource(R.drawable.ic_help),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = { sendIntent(SettingsIntent.OnHowToTap) }
                )

            }

            SettingsSection(
                iconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                backgroundColor = MaterialTheme.colorScheme.surfaceVariant
            ) { iconColor, bgColor ->

                SettingsItem(
                    text = stringResource(R.string.about),
                    value = stringResource(R.string.about_desc),
                    painter = painterResource(R.drawable.ic_info),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = { sendIntent(SettingsIntent.OnAboutTap) }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.make_a_donation),
                    value = stringResource(R.string.support_me_desc),
                    painter = painterResource(R.drawable.ic_money),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = {
                        WebLink.openPage(
                            context = context,
                            url = Constants.CONTACT.BUY_COFFEE
                        )
                    }
                )

            }

            SettingsSection(
                iconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                backgroundColor = MaterialTheme.colorScheme.secondaryContainer
            ) { iconColor, bgColor ->

                SettingsItem(
                    text = stringResource(R.string.x),
                    value = stringResource(R.string.stay_informed),
                    painter = painterResource(R.drawable.ic_social_media),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = {
                        WebLink.openPage(
                            context = context,
                            url = Constants.CONTACT.X
                        )
                    }
                )

                SettingsDivider()

                SettingsItem(
                    text = stringResource(R.string.sources),
                    value = "",
                    painter = painterResource(R.drawable.ic_sources),
                    iconColor = iconColor,
                    backgroundColor = bgColor,
                    onTap = {
                        WebLink.openPage(
                            context = context,
                            url = Constants.CONTACT.GITHUB
                        )
                    }
                )

                appVersion?.let {

                    SettingsDivider()

                    SettingsItem(
                        text = stringResource(R.string.app_version),
                        value = it,
                        painter = painterResource(R.drawable.ic_version),
                        iconColor = iconColor,
                        backgroundColor = bgColor,
                        onTap = {
                            WebLink.openPage(
                                context = context,
                                url = Constants.CONTACT.RELEASES
                            )
                        }
                    )

                }

            }

            Spacer(modifier = Modifier.height(innerPadding.calculateBottomPadding()))

        }

    }

}

@Composable
fun SettingsSection(
    iconColor: Color,
    backgroundColor: Color,
    content: @Composable (Color, Color) -> Unit
) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Ui.Space.MEDIUM)
                .clip(Ui.Shape.Corner.Small)
                .background(MaterialTheme.colorScheme.surfaceContainer),
            horizontalAlignment = Alignment.Start
        ) { content(iconColor, backgroundColor) }
}

@Composable
fun SettingsItem(
    text: String,
    value: String,
    painter: Painter,
    backgroundColor: Color,
    iconColor: Color,
    onTap: () -> Unit
) {

    Row(
        modifier = Modifier
            .clickable { onTap() }
            .fillMaxWidth()
            .padding(all = Ui.Space.MEDIUM),
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
        verticalAlignment = Alignment.CenterVertically
    ) {

        SettingIcon(
            painter = painter,
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            contentDescription = text
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            Text.Title.Medium(
                text = text,
            )

            Text.Title.Small(
                text = value.uppercaseFirstLetter(),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
            )

        }

    }

}

@Composable
fun SettingsSwitch(
    text: String,
    subText: String,
    checked: Boolean,
    painter: Painter,
    backgroundColor: Color,
    iconColor: Color,
    onCheckedChange: (Boolean) -> Unit
) {

    Row(
        modifier = Modifier
            .clickable { onCheckedChange(!checked) }
            .fillMaxWidth()
            .padding(all = Ui.Space.MEDIUM),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM),
    ) {

        SettingIcon(
            painter = painter,
            backgroundColor = backgroundColor,
            iconColor = iconColor,
            contentDescription = text
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = Ui.Space.MEDIUM),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
        ) {

            Text.Title.Medium(
                text = text,
            )

            Text.Title.Small(
                text = subText,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = .8f),
                lineHeight = 18.sp
            )

        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
        )

    }

}

@Composable
fun SettingIcon(
    painter: Painter,
    backgroundColor: Color,
    iconColor: Color,
    contentDescription: String
) {

    Icon(
        modifier = Modifier
            .clip(Ui.Shape.Corner.Full)
            .background(backgroundColor)
            .padding(all = Ui.Space.SMALL),
        painter = painter,
        tint = iconColor,
        contentDescription = contentDescription
    )

}

@Composable
fun SettingsDivider() {

    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background
    )

}

@Composable
fun <T> SettingsDialog(
    state: SettingsDialogState<T>,
    sendIntent: (SettingsIntent) -> Unit,
    onDismiss: () -> Unit
) {

    var selectedValue by remember { mutableStateOf(state.currentValue) }

    FluxDialog(
        onDismiss = onDismiss,
        onValidate = { sendIntent(state.applyValue(selectedValue)) },
        title = stringResource(state.title),
        content = {

            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(Ui.Space.MEDIUM)
            ) {

                state.options.forEach { option ->

                    Row(
                        modifier = Modifier
                            .clickable { selectedValue = option.key  }
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Ui.Space.EXTRA_SMALL)
                    ) {

                        RadioButton(
                            selected = selectedValue == option.key,
                            onClick = { selectedValue = option.key }
                        )

                        val value = option.value.second?.let { stringResource(it) } ?: option.value.first
                        Text.Body.Large(
                            modifier = Modifier.weight(1f),
                            text = value.uppercaseFirstLetter(),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                    }

                }

            }

        }
    )

}

@FluxPreview
@Composable
fun SettingsScreen_Preview() {
    AppTheme {
        SettingsContent(
            state = SettingsUiState(),
            context = LocalContext.current,
            appVersion = "1.0.0",
        ) { }
    }
}

@FluxPreview
@Composable
fun SettingsDialog_Preview() {
    AppTheme {
        SettingsDialog(
            state = SettingsDialogState.forward(5),
            sendIntent = {},
            onDismiss = {}
        )
    }
}

