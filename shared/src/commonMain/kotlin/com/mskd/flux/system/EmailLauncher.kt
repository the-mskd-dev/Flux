package com.mskd.flux.system

interface EmailLauncher {
    fun sendEmail(to: String ="the.masked.dev@proton.me", subject: String = "", body: String = "")
}