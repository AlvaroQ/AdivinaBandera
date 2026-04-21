package com.alvaroquintana.adivinabandera.ui.profile.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffFamily
import com.alvaroquintana.adivinabandera.ui.theme.DynaPuffSemiCondensedFamily
import com.alvaroquintana.adivinabandera.ui.theme.ElevationTokens
import com.alvaroquintana.adivinabandera.ui.theme.GameGold
import com.alvaroquintana.adivinabandera.ui.theme.GeoNavySoftLight
import com.alvaroquintana.adivinabandera.ui.theme.LocalWindowSizeClass
import com.alvaroquintana.adivinabandera.ui.theme.MotionTokens
import com.alvaroquintana.adivinabandera.ui.theme.PillShape
import com.alvaroquintana.adivinabandera.ui.theme.isAppInDarkTheme
import com.alvaroquintana.adivinabandera.ui.theme.isExpanded
import com.alvaroquintana.adivinabandera.ui.theme.isMedium

/**
 * App-wide blue accent — matches the NavigationBar selected state and the
 * primary Play-tab cards. Single source of truth for the Profile hero.
 */
@Composable
private fun appAccentColor(): Color =
    if (isAppInDarkTheme()) MaterialTheme.colorScheme.primary else GeoNavySoftLight

/**
 * Profile Hero — Material 3 Expressive two-layer pattern (Option C):
 *
 *   ┌─ Identity surface (tier-tinted gradient) ─┐
 *   │  [avatar]  Nickname  ✎                    │
 *   │            Entusiasta                     │
 *   └───────────────────────────────────────────┘
 *   ┌─ Progress ticker (surfaceContainerLow) ───┐
 *   │  Lv 12  ━━━━━━━━━━━━━━  62%   #42 🌍     │
 *   │         1240 / 2000 XP                    │
 *   └───────────────────────────────────────────┘
 *
 * Each layer has a distinct role: identity (who I am) vs progress (how I'm doing).
 * Tap on the avatar opens the image source sheet — no competing FAB.
 */
@Composable
fun ProfileHeroSection(
    nickname: String,
    imageBase64: String,
    level: Int,
    totalXp: Int,
    xpProgressInLevel: Int,
    xpNeededForLevel: Int,
    xpForNextLevel: Int,
    globalRank: Int,
    onNicknameChange: (String) -> Unit,
    onEditImageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val windowSize = LocalWindowSizeClass.current
    val wide = windowSize.isMedium || windowSize.isExpanded

    val tier = remember(level) { LevelTier.fromLevel(level) }
    val accent = appAccentColor()

    val progress = remember(xpProgressInLevel, xpNeededForLevel) {
        if (xpNeededForLevel > 0) {
            (xpProgressInLevel.toFloat() / xpNeededForLevel).coerceIn(0f, 1f)
        } else 1f
    }
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(
            durationMillis = MotionTokens.DurationExtraLong1,
            easing = MotionTokens.EmphasizedDecelerate
        ),
        label = "xp-progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = ElevationTokens.Level2)
    ) {
        Column {
            IdentitySurface(
                nickname = nickname,
                imageBase64 = imageBase64,
                tier = tier,
                accent = accent,
                wide = wide,
                onNicknameChange = onNicknameChange,
                onEditImageClick = onEditImageClick
            )
            ProgressTicker(
                level = level,
                totalXp = totalXp,
                xpProgressInLevel = xpProgressInLevel,
                xpNeededForLevel = xpNeededForLevel,
                xpForNextLevel = xpForNextLevel,
                globalRank = globalRank,
                progress = animatedProgress,
                accent = accent,
                wide = wide
            )
        }
    }
}

// ── Layer 1: Identity surface ─────────────────────────────────────────────────
@Composable
private fun IdentitySurface(
    nickname: String,
    imageBase64: String,
    tier: LevelTier,
    accent: Color,
    wide: Boolean,
    onNicknameChange: (String) -> Unit,
    onEditImageClick: () -> Unit
) {
    val avatarSize = if (wide) 96.dp else 76.dp

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    0f to accent.copy(alpha = 0.22f),
                    1f to accent.copy(alpha = 0.06f)
                )
            )
            .padding(horizontal = 20.dp, vertical = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Avatar(
                imageBase64 = imageBase64,
                size = avatarSize,
                accent = accent,
                onClick = onEditImageClick
            )

            Column(modifier = Modifier.weight(1f)) {
                NicknameInline(
                    nickname = nickname,
                    accent = accent,
                    onNicknameChange = onNicknameChange
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tier.displayName,
                    fontFamily = DynaPuffSemiCondensedFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = accent,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ── Layer 2: Progress ticker ──────────────────────────────────────────────────
@Composable
private fun ProgressTicker(
    level: Int,
    totalXp: Int,
    xpProgressInLevel: Int,
    xpNeededForLevel: Int,
    xpForNextLevel: Int,
    globalRank: Int,
    progress: Float,
    accent: Color,
    wide: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Level pill
            Surface(
                shape = PillShape,
                color = accent,
                modifier = Modifier.height(28.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp)
                ) {
                    Text(
                        text = "Lv $level",
                        fontFamily = DynaPuffCondensedFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp,
                        color = Color.White
                    )
                }
            }

            // Progress bar — the hero's "how I'm doing"
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .weight(1f)
                    .height(10.dp)
                    .clip(PillShape),
                color = accent,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
            )

            // Rank chip (right-anchored)
            if (globalRank > 0) {
                RankChip(rank = globalRank)
            }
        }

        Spacer(Modifier.height(6.dp))

        // Secondary line: XP numbers + next level hint
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "$xpProgressInLevel / $xpNeededForLevel XP",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = if (level < 50) "Siguiente: $xpForNextLevel XP"
                       else "Nivel máximo",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 12.sp,
                color = if (level < 50) MaterialTheme.colorScheme.onSurfaceVariant else GameGold
            )
        }

        if (wide) {
            Spacer(Modifier.height(2.dp))
            Text(
                text = "$totalXp XP totales",
                fontFamily = DynaPuffSemiCondensedFamily,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── Avatar (tap opens sheet; tiny camera marker signals affordance) ───────────
@Composable
private fun Avatar(
    imageBase64: String,
    size: Dp,
    accent: Color,
    onClick: () -> Unit
) {
    val bitmap = remember(imageBase64) { imageBase64.decodeBase64Bitmap() }
    val markerSize = (size.value * 0.32f).dp

    Box(
        modifier = Modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        // Avatar surface — clickable to edit
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier
                .size(size)
                .clip(CircleShape)
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.AccountCircle,
                        contentDescription = "Avatar vacío",
                        tint = accent,
                        modifier = Modifier.fillMaxSize().padding(6.dp)
                    )
                }
            }
        }

        // Tiny camera marker — bottom-end — signals "tap to edit"
        Box(
            modifier = Modifier.size(size),
            contentAlignment = Alignment.BottomEnd
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = ElevationTokens.Level2,
                modifier = Modifier
                    .size(markerSize)
                    .clickable(onClick = onClick)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.CameraAlt,
                        contentDescription = "Cambiar imagen",
                        tint = accent,
                        modifier = Modifier.size(markerSize * 0.55f)
                    )
                }
            }
        }
    }
}

// ── Nickname inline editor ────────────────────────────────────────────────────
@Composable
private fun NicknameInline(
    nickname: String,
    accent: Color,
    onNicknameChange: (String) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var draft by remember(nickname) { mutableStateOf(nickname) }
    val focusManager = LocalFocusManager.current

    AnimatedContent(
        targetState = isEditing,
        transitionSpec = {
            (fadeIn(tween(MotionTokens.DurationMedium2, easing = MotionTokens.EmphasizedDecelerate))
                + scaleIn(
                    initialScale = 0.94f,
                    animationSpec = tween(MotionTokens.DurationMedium2, easing = MotionTokens.EmphasizedDecelerate)
                )) togetherWith
                (fadeOut(tween(MotionTokens.DurationShort4, easing = MotionTokens.EmphasizedAccelerate))
                    + scaleOut(
                        targetScale = 0.96f,
                        animationSpec = tween(MotionTokens.DurationShort4, easing = MotionTokens.EmphasizedAccelerate)
                    ))
        },
        label = "nickname-editor"
    ) { editing ->
        if (editing) {
            OutlinedTextField(
                value = draft,
                onValueChange = {
                    draft = it
                    onNicknameChange(it)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        isEditing = false
                    }
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accent,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                ),
                trailingIcon = {
                    Surface(
                        onClick = {
                            focusManager.clearFocus()
                            isEditing = false
                        },
                        shape = CircleShape,
                        color = accent
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = "Guardar",
                            tint = Color.White,
                            modifier = Modifier
                                .padding(6.dp)
                                .size(16.dp)
                        )
                    }
                },
                modifier = Modifier.widthIn(min = 180.dp).fillMaxWidth()
            )
        } else {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = nickname.ifBlank { "Agregar apodo" },
                    fontFamily = DynaPuffFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = if (nickname.isBlank())
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Surface(
                    onClick = { isEditing = true },
                    shape = CircleShape,
                    color = Color.Transparent
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "Editar apodo",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(4.dp).size(16.dp)
                    )
                }
            }
        }
    }
}

// ── Rank chip (only shown when user has ranked) ───────────────────────────────
@Composable
private fun RankChip(rank: Int) {
    Surface(
        shape = PillShape,
        color = GameGold.copy(alpha = 0.18f),
        modifier = Modifier.height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Public,
                contentDescription = null,
                tint = GameGold,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "#$rank",
                fontFamily = DynaPuffCondensedFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = GameGold
            )
        }
    }
}
