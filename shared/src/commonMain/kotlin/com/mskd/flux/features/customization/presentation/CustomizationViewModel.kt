package com.mskd.flux.features.customization.presentation

import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.core.model.core.FluxOptionsDialogItem
import com.mskd.flux.core.model.core.FluxOptionsDialogState
import com.mskd.flux.core.model.core.StringProvider
import com.mskd.flux.features.customization.domain.datastore.CustomizationDataStore
import com.mskd.flux.features.customization.domain.model.CustomizationDialog
import com.mskd.flux.features.customization.domain.model.NavigationStyle
import com.mskd.flux.utils.UiCommon
import flux.shared.generated.resources.Res
import flux.shared.generated.resources.accent_color
import flux.shared.generated.resources.app_theme
import flux.shared.generated.resources.items_per_row
import flux.shared.generated.resources.items_per_row_desc
import flux.shared.generated.resources.navigation_style
import flux.shared.generated.resources.seasons_per_row
import flux.shared.generated.resources.seasons_per_row_desc
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CustomizationViewModel(
    private val customizationDataStore: CustomizationDataStore,
) : ViewModel() {

    private val _dialogState = MutableStateFlow<CustomizationDialog?>(null)

    val uiState: StateFlow<CustomizationUiState> = combine(
        customizationDataStore.flow,
        _dialogState
    ) { customization, dialog ->
        CustomizationUiState(
            uiTheme = customization.uiTheme,
            color = customization.color,
            waveProgress = customization.waveProgress,
            oldBlurredHeader = customization.oldBlurredHeader,
            largeEpisodeImage = customization.largeEpisodeImage,
            itemsPerRow = customization.itemsPerRow,
            itemsCorners = customization.itemsCorners,
            seasonsPerRow = customization.seasonsPerRow,
            navigationStyle = customization.navigationStyle,
            dialog = dialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CustomizationUiState()
    )

    private val _event = MutableSharedFlow<CustomizationEvent>()
    val event = _event.asSharedFlow()

    //endregion

    //region Intents

    fun handleIntent(intent: CustomizationIntent) = viewModelScope.launch {
        when (intent) {

            // Global
            CustomizationIntent.OnBackTap -> _event.emit(CustomizationEvent.BackToPreviousScreen)

            // Dialogs
            CustomizationIntent.HideDialog -> hideDialog()
            CustomizationIntent.ShowColorDialog -> showColorDialog()
            CustomizationIntent.ShowThemeDialog -> showThemeDialog()
            CustomizationIntent.ShowItemsPerRowDialog -> showItemsPerRowDialog()
            CustomizationIntent.ShowItemsCornerDialog -> showItemsCornersDialog()
            CustomizationIntent.ShowSeasonsPerRowDialog -> showSeasonsPerRowDialog()
            CustomizationIntent.ShowNavigationStyleDialog -> showNavigationStyleDialog()


            // Setters
            is CustomizationIntent.SetColorValue -> setColor(color = intent.color)
            is CustomizationIntent.SetThemeValue -> setTheme(theme = intent.theme)
            is CustomizationIntent.SetItemsPerRowValue -> setItemsPerRowValue(count = intent.count)
            is CustomizationIntent.SetItemsCornersValue -> setItemsCornersValue(corners = intent.corners)
            is CustomizationIntent.SetSeasonsPerRowValue -> setSeasonsPerRowValue(count = intent.count)
            is CustomizationIntent.OnWaveProgressCheck -> setWaveProgress(waveProgress = intent.checked)
            is CustomizationIntent.OnOldBlurredHeaderCheck -> setOldBlurredHeader(blurred = intent.checked)
            is CustomizationIntent.OnLargeEpisodeImageCheck -> setLargeEpisodeImage(large = intent.checked)
            is CustomizationIntent.SetNavigationStyle -> setNavigationStyle(style = intent.style)
        }
    }

    //endregion

    //region Private Methods

    private fun hideDialog() {
        _dialogState.update { null }
    }

    private fun showThemeDialog() {
        val currentValue = uiState.value.uiTheme
        val dialogState = FluxOptionsDialogState(
            titleResId = Res.string.app_theme,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = UiCommon.THEME.LIGHT, label = StringProvider.Resource(UiCommon.THEME.LIGHT.stringResource)),
                FluxOptionsDialogItem(value = UiCommon.THEME.DARK, label = StringProvider.Resource(UiCommon.THEME.DARK.stringResource)),
                FluxOptionsDialogItem(value = UiCommon.THEME.SYSTEM, label = StringProvider.Resource(UiCommon.THEME.SYSTEM.stringResource))
            ),
            applyValue = { value -> CustomizationIntent.SetThemeValue(value) }
        )

        _dialogState.update { CustomizationDialog.SelectDialog(state = dialogState) }
    }

    private suspend fun setTheme(theme: UiCommon.THEME) {
        customizationDataStore.setUiTheme(theme)
        hideDialog()
    }

    private fun showColorDialog() {
        val currentValue = uiState.value.color
        val dialogState = FluxOptionsDialogState(
            titleResId = Res.string.accent_color,
            currentValue = currentValue,
            options = listOf(
                UiCommon.AccentColors.System.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
                UiCommon.AccentColors.Red.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
                UiCommon.AccentColors.Blue.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
                UiCommon.AccentColors.Green.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
                UiCommon.AccentColors.Yellow.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
                UiCommon.AccentColors.Magenta.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
                UiCommon.AccentColors.Gray.let { FluxOptionsDialogItem(value = it.color?.toArgb(), label = StringProvider.Resource(it.stringResId), color = it.color) },
            ),
            applyValue = { value -> CustomizationIntent.SetColorValue(value) }
        )

        _dialogState.update { CustomizationDialog.SelectDialog(state = dialogState) }
    }

    private fun showNavigationStyleDialog() {
        val currentValue = uiState.value.navigationStyle
        val dialogState = FluxOptionsDialogState(
            titleResId = Res.string.navigation_style,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = NavigationStyle.PILL, label = NavigationStyle.PILL.description),
                FluxOptionsDialogItem(value = NavigationStyle.BOTTOM_BAR, label = NavigationStyle.BOTTOM_BAR.description),
                FluxOptionsDialogItem(value = NavigationStyle.TOP_BAR, label = NavigationStyle.TOP_BAR.description),
            ),
            applyValue = { value -> CustomizationIntent.SetNavigationStyle(value) }
        )

        _dialogState.update { CustomizationDialog.SelectDialog(state = dialogState) }
    }

    private suspend fun setNavigationStyle(style: NavigationStyle) {
        customizationDataStore.setNavigationStyle(style)
        hideDialog()
    }

    private fun showItemsPerRowDialog() {
        _dialogState.update {
            CustomizationDialog.ItemsPerRowDialog(
                title = StringProvider.Resource(Res.string.items_per_row),
                desc = StringProvider.Resource(Res.string.items_per_row_desc),
            )
        }
    }

    private fun showItemsCornersDialog() {
        _dialogState.update {
            CustomizationDialog.ItemsCornersDialog(
                title = StringProvider.Resource(Res.string.items_per_row),
                desc = StringProvider.Resource(Res.string.items_per_row_desc),
            )
        }
    }

    private fun showSeasonsPerRowDialog() {
        _dialogState.update {
            CustomizationDialog.SeasonsPerRowDialog(
                title = StringProvider.Resource(Res.string.seasons_per_row),
                desc = StringProvider.Resource(Res.string.seasons_per_row_desc),
            )
        }
    }

    private suspend fun setColor(color: Int?) {
        customizationDataStore.setColor(color)
        hideDialog()
    }

    private suspend fun setWaveProgress(waveProgress: Boolean) {
        customizationDataStore.setWaveProgress(waveProgress)
    }

    private suspend fun setOldBlurredHeader(blurred: Boolean) {
        customizationDataStore.setOldBlurredHeader(blurred)
    }

    private suspend fun setLargeEpisodeImage(large: Boolean) {
        customizationDataStore.setLargeEpisodeImage(large)
    }

    private suspend fun setItemsPerRowValue(count: Int) {
        customizationDataStore.setItemsPerRow(count)
        hideDialog()
    }

    private suspend fun setItemsCornersValue(corners: Int) {
        customizationDataStore.setItemsCorners(corners)
        hideDialog()
    }

    private suspend fun setSeasonsPerRowValue(count: Int) {
        customizationDataStore.setSeasonsPerRow(count)
        hideDialog()
    }

    //endregion

}