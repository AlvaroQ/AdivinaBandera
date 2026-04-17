package com.alvaroquintana.adivinabandera.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.alvaroquintana.adivinabandera.R
import com.alvaroquintana.adivinabandera.ui.result.MysteryBoxReward
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavy
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import kotlinx.coroutines.delay

/**
 * Dialog de caja misteriosa que aparece tras cada 10a partida.
 *
 * Flujo de animacion:
 * 1. Muestra "?" durante 1200ms (estado oculto)
 * 2. Transicion fade+scale revela el contenido real
 *
 * [reward] contiene xpAmount, coinsAmount y type.
 * [onDismiss] cierra el dialog.
 */
@Composable
fun MysteryBoxDialog(
    reward: MysteryBoxReward,
    onDismiss: () -> Unit
) {
    val isDark = isAppInDarkTheme()
    val cardBg = if (isDark) Color(0xFF0E1628) else Color.White
    val textPrimary = if (isDark) Color(0xFFD0DAEA) else Color(0xFF1A1A2E)
    val textMuted = if (isDark) Color(0xFF636E80) else Color(0xFF9CA3AF)

    var revealed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1200L)
        revealed = true
    }

    Dialog(onDismissRequest = if (revealed) onDismiss else { {} }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = cardBg,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = stringResource(R.string.mystery_box_title),
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = GameGold,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = stringResource(R.string.mystery_box_completed_games),
                    fontSize = 13.sp,
                    color = textMuted,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                AnimatedContent(
                    targetState = revealed,
                    transitionSpec = {
                        (scaleIn(tween(400)) + fadeIn(tween(400))) togetherWith
                            fadeOut(tween(200))
                    },
                    label = "mystery_reveal"
                ) { isRevealed ->
                    if (!isRevealed) {
                        // Estado oculto: signo de pregunta
                        Text(
                            text = "?",
                            fontFamily = DynaPuffFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 56.sp,
                            color = GameGold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        // Estado revelado: recompensas
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = when (reward.type) {
                                    MysteryBoxReward.Type.XP_BONUS -> "\u26A1"
                                    MysteryBoxReward.Type.COINS_BONUS -> "\uD83E\uDE99"
                                    MysteryBoxReward.Type.FREEZE_TOKEN -> "\u2744\uFE0F"
                                },
                                fontSize = 52.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (reward.xpAmount > 0) {
                                RewardRow(
                                    icon = "\u26A1",
                                    text = "+${reward.xpAmount} XP",
                                    color = Color(0xFF7C3AED)
                                )
                            }
                            if (reward.coinsAmount > 0) {
                                RewardRow(
                                    icon = "\uD83E\uDE99",
                                    text = stringResource(R.string.mystery_box_coins, reward.coinsAmount),
                                    color = GameGold
                                )
                            }
                            if (reward.type == MysteryBoxReward.Type.FREEZE_TOKEN) {
                                RewardRow(
                                    icon = "\u2744\uFE0F",
                                    text = stringResource(R.string.mystery_box_freeze_token),
                                    color = Color(0xFF06B6D4)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                if (revealed) {
                    Surface(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(20.dp),
                        color = GeoNavy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.mystery_box_great),
                            fontFamily = DynaPuffFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.mystery_box_opening),
                        fontSize = 12.sp,
                        color = textMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun RewardRow(icon: String, text: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = icon, fontSize = 18.sp)
        Text(
            text = text,
            fontFamily = DynaPuffFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = color
        )
    }
}
