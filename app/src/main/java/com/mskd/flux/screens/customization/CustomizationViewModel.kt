package com.mskd.flux.screens.customization

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.R
import com.mskd.flux.data.repository.customization.CustomizationRepository
import com.mskd.flux.screens.customization.composables.ColorItem
import com.mskd.flux.ui.component.FluxOptionsDialogItem
import com.mskd.flux.ui.component.FluxOptionsDialogState
import com.mskd.flux.ui.theme.Ui
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomizationViewModel @Inject constructor(
    application: Application,
    private val customizationRepository: CustomizationRepository
) : AndroidViewModel(application) {

    //region Variables

    private val context = getApplication<Application>()

    private val _dialogState = MutableStateFlow<FluxOptionsDialogState<*, CustomizationIntent>?>(null)

    val uiState: StateFlow<CustomizationUiState> = combine(
        customizationRepository.flow,
        _dialogState
    ) { customization, dialog ->
        CustomizationUiState(
            uiTheme = customization.uiTheme,
            color = customization.color,
            dialogState = dialog
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


            // Setters
            is CustomizationIntent.SetColorValue -> setColor(color = intent.color)
            is CustomizationIntent.SetThemeValue -> setTheme(theme = intent.theme)

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
            titleResId = R.string.app_theme,
            currentValue = currentValue,
            options = listOf(
                FluxOptionsDialogItem(value = Ui.THEME.LIGHT, label = context.getString(Ui.THEME.LIGHT.stringResourceId)),
                FluxOptionsDialogItem(value = Ui.THEME.DARK, label = context.getString(Ui.THEME.DARK.stringResourceId)),
                FluxOptionsDialogItem(value = Ui.THEME.SYSTEM, label = context.getString(Ui.THEME.SYSTEM.stringResourceId))
            ),
            applyValue = { value -> CustomizationIntent.SetThemeValue(value) }
        )

        _dialogState.update { dialogState }
    }

    private suspend fun setTheme(theme: Ui.THEME) {
        customizationRepository.setUiTheme(theme)
        hideDialog()
    }

    private fun showColorDialog() {
        val currentValue = uiState.value.color
        val dialogState = FluxOptionsDialogState(
            titleResId = R.string.app_theme,
            currentValue = currentValue,
            options = listOf(
                Ui.Colors.System.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
                Ui.Colors.Red.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
                Ui.Colors.Blue.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
                Ui.Colors.Green.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
                Ui.Colors.Yellow.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
                Ui.Colors.Magenta.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
                Ui.Colors.Gray.let { FluxOptionsDialogItem(value = it.argb, label = context.getString(it.stringResId), left = { ColorItem(it.argb) } ) },
            ),
            applyValue = { value -> CustomizationIntent.SetColorValue(value) }
        )

        _dialogState.update { dialogState }
    }

    private suspend fun setColor(color: Int?) {
        customizationRepository.setColor(color)
        hideDialog()
    }

    //endregion

}