package com.mskd.flux.platform

interface EmailLauncher {
    fun sendEmail(to: String ="the.masked.dev@proton.me", subject: String = "", body: String = "")
}