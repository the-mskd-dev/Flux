package com.mskd.flux.system

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import com.mskd.flux.utils.Trace

class AndroidEmailLauncher(private val context: Context) : EmailLauncher {

    override fun sendEmail(to: String, subject: String, body: String) {

        val uriText = "mailto:${Uri.encode(to)}" +
                "?subject=${Uri.encode(subject)}" +
                "&body=${Uri.encode(body)}"

        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = uriText.toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Trace.error("AndroidEmailLauncher", "Error sending email", e)
        }

    }

}