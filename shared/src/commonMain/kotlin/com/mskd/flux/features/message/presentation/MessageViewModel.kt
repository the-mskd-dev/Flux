package com.mskd.flux.features.message.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mskd.flux.system.EmailLauncher
import kotlinx.coroutines.launch

class MessageViewModel(private val emailLauncher: EmailLauncher) : ViewModel() {

    fun handleIntent(intent: MessageIntent) = viewModelScope.launch {
        when (intent) {
            is MessageIntent.OnActionClick -> sendEmail()
        }
    }

    private fun sendEmail() {
        emailLauncher.sendEmail(
            subject = "Play Store testing",
            body = "Hi, I want to help you to test Flux on the Play Store, kisses!"
        )
    }
}