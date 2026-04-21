package com.alvaroquintana.adivinabandera.ui.profile.components

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream

internal fun String.decodeBase64Bitmap(): Bitmap? {
    if (isBlank()) return null
    return runCatching {
        val bytes = Base64.decode(this, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }.getOrNull()
}

internal fun Bitmap.toBase64(maxEdge: Int = 512, quality: Int = 80): String {
    val maxDimension = width.coerceAtLeast(height)
    val scaled = if (maxDimension > maxEdge) {
        val scale = maxEdge.toFloat() / maxDimension.toFloat()
        Bitmap.createScaledBitmap(
            this,
            (width * scale).toInt().coerceAtLeast(1),
            (height * scale).toInt().coerceAtLeast(1),
            true
        )
    } else {
        this
    }

    return ByteArrayOutputStream().use { output ->
        scaled.compress(Bitmap.CompressFormat.JPEG, quality, output)
        Base64.encodeToString(output.toByteArray(), Base64.DEFAULT)
    }
}

internal fun uriToBase64(context: Context, uri: Uri): String? = runCatching {
    context.contentResolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input)?.toBase64()
    }
}.getOrNull()
