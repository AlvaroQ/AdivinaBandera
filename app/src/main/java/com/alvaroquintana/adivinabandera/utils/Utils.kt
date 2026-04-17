package com.alvaroquintana.adivinabandera.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.alvaroquintana.adivinabandera.BuildConfig
import com.alvaroquintana.adivinabandera.R
import androidx.core.net.toUri


fun shareApp(context: Context, points: Int) {
    try {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
        var shareMessage = if (points != -1) {
            context.resources.getString(R.string.share_message, points)
        } else {
            context.resources.getString(R.string.share_message_general)
        }
        shareMessage =
            """
                ${shareMessage}https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}
                """.trimIndent()
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.choose_one)))
    } catch (e: Exception) {
        log(context.getString(R.string.share), e.toString())
    }
}

fun rateApp(context: Context) {
    val uri: Uri = "market://details?id=${BuildConfig.APPLICATION_ID}".toUri()
    val goToMarket = Intent(Intent.ACTION_VIEW, uri)
    goToMarket.addFlags(
        Intent.FLAG_ACTIVITY_NO_HISTORY or
                Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
    try {
        context.startActivity(goToMarket)
    } catch (_: ActivityNotFoundException) {
        context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                "https://play.google.com/store/apps/details?id=${BuildConfig.APPLICATION_ID}".toUri())
        )
    }
}

fun log(tag: String?, msg: String?, error: Throwable? = null) {
    if (BuildConfig.DEBUG) {
        if (error != null) {
            Log.e(tag, msg, error)
        } else {
            Log.d(tag, msg!!)
        }
    }
}
