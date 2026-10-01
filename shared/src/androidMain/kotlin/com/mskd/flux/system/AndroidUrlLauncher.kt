package com.mskd.flux.system

import android.content.Context
import android.content.Intent
import androidx.core.net.toUri

class AndroidUrlLauncher(private val context: Context) : UrlLauncher {

    override fun open(url: String) {
        val uri = url.toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

}