package com.alvaroquintana.adivinabandera.ui.composables

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.alvaroquintana.adivinabandera.BuildConfig
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BrokenImage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import coil3.compose.AsyncImage

/**
 * Unified flag image component.
 * Prioritizes base64 decoding; falls back to URL loading via Coil.
 */
@Composable
fun FlagImage(
    base64: String = "",
    url: String = "",
    contentDescription: String? = null,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    when {
        base64.isNotBlank() -> {
            val bitmap = remember(base64) {
                try {
                    val bytes = Base64.decode(base64, Base64.DEFAULT)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (e: Exception) {
                    if (BuildConfig.DEBUG) {
                        Log.e("FlagImage", "Failed to decode base64 flag image", e)
                    }
                    null
                }
            }

            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = contentDescription,
                    modifier = modifier,
                    contentScale = contentScale
                )
            } else {
                FlagImageError(modifier = modifier)
            }
        }

        url.isNotBlank() -> {
            AsyncImage(
                model = url,
                contentDescription = contentDescription,
                modifier = modifier,
                contentScale = contentScale
            )
        }

        else -> {
            FlagImageError(modifier = modifier)
        }
    }
}

@Composable
private fun FlagImageError(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.BrokenImage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
