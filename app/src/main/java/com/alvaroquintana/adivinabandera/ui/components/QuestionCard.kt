package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.composables.FlagImage

@Composable
fun QuestionCard(
    flagBase64: String = "",
    flagUrl: String = "",
    questionNumber: Int,
    totalQuestions: Int,
    modifier: Modifier = Modifier,
    imageContentScale: ContentScale = ContentScale.Fit
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            FlagImage(
                base64 = flagBase64,
                url = flagUrl,
                contentDescription = stringResource(
                    R.string.game_image
                ),
                modifier = Modifier.fillMaxSize(),
                contentScale = imageContentScale
            )
        }
    }
}
